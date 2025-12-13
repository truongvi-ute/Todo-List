package com.mycompany.controller;

import com.mycompany.data.DeadlineTaskDB;
import com.mycompany.model.DayInfo;
import com.mycompany.model.DeadlineTask;
import com.mycompany.model.Priority;
import com.mycompany.model.Status;
import com.mycompany.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/deadline"})
public class DeadlineServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Kiểm tra đăng nhập
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }
        
        User user = (User) session.getAttribute("loginedUser");
        
        // Lấy week offset từ session (để chuyển tuần)
        Integer weekOffset = (Integer) session.getAttribute("weekOffset");
        if (weekOffset == null) {
            weekOffset = 0;
        }
        
        // Xử lý action chuyển tuần từ GET request
        String action = request.getParameter("action");
        if ("prevWeek".equals(action)) {
            weekOffset--;
            session.setAttribute("weekOffset", weekOffset);
        } else if ("nextWeek".equals(action)) {
            weekOffset++;
            session.setAttribute("weekOffset", weekOffset);
        }
        
        // Lấy filter và sort params (ưu tiên từ request, nếu không có thì lấy từ session)
        String filter = request.getParameter("filter");
        String sort = request.getParameter("sort");
        
        // Nếu có param mới từ request -> lưu vào session
        if (filter != null) {
            session.setAttribute("deadlineFilter", filter);
        } else {
            // Lấy từ session, nếu không có thì mặc định "all"
            filter = (String) session.getAttribute("deadlineFilter");
            if (filter == null) filter = "all";
        }
        
        if (sort != null) {
            session.setAttribute("deadlineSort", sort);
        } else {
            // Lấy từ session, nếu không có thì mặc định "date"
            sort = (String) session.getAttribute("deadlineSort");
            if (sort == null) sort = "date";
        }
        
        // Tính toán tuần hiện tại (Thứ 2 -> Chủ nhật)
        LocalDate today = LocalDate.now().plusWeeks(weekOffset);
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        
        // Tạo danh sách 7 ngày trong tuần
        List<DayInfo> weekDays = buildWeekDays(monday);
        
        // Query tasks từ database
        LocalDateTime startDateTime = monday.atStartOfDay();
        LocalDateTime endDateTime = sunday.plusDays(1).atStartOfDay();
        
        List<DeadlineTask> tasks;
        if ("priority".equals(sort)) {
            tasks = DeadlineTaskDB.getTasksByUserDateRangeSortByPriority(user, startDateTime, endDateTime);
        } else if (!"all".equals(filter)) {
            tasks = DeadlineTaskDB.getTasksByUserDateRangeAndStatus(user, startDateTime, endDateTime, filter);
        } else {
            tasks = DeadlineTaskDB.getTasksByUserAndDateRange(user, startDateTime, endDateTime);
        }
        
        // Cập nhật status LATE cho các task quá hạn (chưa DONE và đã qua ngày)
        LocalDate realToday = LocalDate.now();
        for (DeadlineTask task : tasks) {
            if (task.getStatus() != Status.DONE && task.getDueDate().toLocalDate().isBefore(realToday)) {
                task.setStatus(Status.LATE);
                DeadlineTaskDB.update(task);
            }
        }
        
        // Gán tasks vào đúng ngày
        Map<LocalDate, List<DeadlineTask>> tasksByDate = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getDueDate().toLocalDate()));
        
        for (DayInfo day : weekDays) {
            List<DeadlineTask> dayTasks = tasksByDate.get(day.getDate());
            if (dayTasks != null) {
                day.setTasks(dayTasks);
            }
        }
        
        // Set attributes cho JSP
        request.setAttribute("weekDays", weekDays);
        request.setAttribute("currentMonth", today.getMonthValue());
        request.setAttribute("currentYear", today.getYear());
        request.setAttribute("currentFilter", filter);
        request.setAttribute("currentSort", sort);
        request.setAttribute("today", realToday);
        request.setAttribute("todayStr", realToday.toString()); // yyyy-MM-dd cho input date min
        
        getServletContext().getRequestDispatcher("/deadline.jsp").forward(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        // Kiểm tra đăng nhập
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }
        
        User user = (User) session.getAttribute("loginedUser");
        String action = request.getParameter("action");
        
        switch (action) {
            case "add":
                handleAddTask(request, user);
                break;
            case "edit":
                handleEditTask(request, user);
                break;
            case "delete":
                handleDeleteTask(request, user);
                break;
            case "toggleStatus":
                handleToggleStatus(request);
                break;
            default:
                break;
        }
        
        // Redirect về GET để refresh trang
        response.sendRedirect(request.getContextPath() + "/deadline");
    }

    // Xử lý thêm task mới
    private void handleAddTask(HttpServletRequest request, User user) {
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String dueDateStr = request.getParameter("dueDate");
        String priorityStr = request.getParameter("priority");
        
        if (title != null && !title.trim().isEmpty() && dueDateStr != null) {
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            
            // Không cho thêm task vào quá khứ
            if (dueDate.isBefore(LocalDate.now())) {
                return; // Bỏ qua, không thêm
            }
            
            LocalDateTime dueDateTime = dueDate.atTime(23, 59); // Cuối ngày
            
            Priority priority = Priority.valueOf(priorityStr);
            
            DeadlineTask task = new DeadlineTask(title.trim(), description, dueDateTime, priority, user);
            DeadlineTaskDB.insert(task);
        }
    }

    // Xử lý chỉnh sửa task
    private void handleEditTask(HttpServletRequest request, User user) {
        String taskIdStr = request.getParameter("taskId");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String dueDateStr = request.getParameter("dueDate");
        String priorityStr = request.getParameter("priority");
        
        if (taskIdStr != null && title != null && !title.trim().isEmpty() && dueDateStr != null) {
            Long taskId = Long.parseLong(taskIdStr);
            DeadlineTask task = DeadlineTaskDB.findById(taskId);
            
            if (task != null && task.getUser().getId().equals(user.getId())) {
                // Không cho sửa task LATE
                if (task.getStatus() == Status.LATE) {
                    return;
                }
                
                LocalDate dueDate = LocalDate.parse(dueDateStr);
                
                // Không cho sửa sang ngày quá khứ
                if (dueDate.isBefore(LocalDate.now())) {
                    return;
                }
                
                task.setTitle(title.trim());
                task.setDescription(description);
                task.setDueDate(dueDate.atTime(23, 59));
                task.setPriority(Priority.valueOf(priorityStr));
                
                DeadlineTaskDB.update(task);
            }
        }
    }

    // Xử lý xóa task
    private void handleDeleteTask(HttpServletRequest request, User user) {
        String taskIdStr = request.getParameter("taskId");
        
        if (taskIdStr != null) {
            Long taskId = Long.parseLong(taskIdStr);
            DeadlineTask task = DeadlineTaskDB.findById(taskId);
            
            // Chỉ cho xóa task của chính user đó và không phải LATE
            if (task != null && task.getUser().getId().equals(user.getId()) && task.getStatus() != Status.LATE) {
                DeadlineTaskDB.delete(taskId);
            }
        }
    }

    // Xử lý toggle trạng thái DONE/IN_PROGRESS
    private void handleToggleStatus(HttpServletRequest request) {
        String taskIdStr = request.getParameter("taskId");
        
        if (taskIdStr != null) {
            Long taskId = Long.parseLong(taskIdStr);
            DeadlineTask task = DeadlineTaskDB.findById(taskId);
            
            if (task != null) {
                // Không cho tick task của quá khứ (đã LATE)
                LocalDate taskDate = task.getDueDate().toLocalDate();
                if (taskDate.isBefore(LocalDate.now())) {
                    return; // Bỏ qua, không toggle
                }
                
                if (task.getStatus() == Status.DONE) {
                    task.setStatus(Status.IN_PROGRESS);
                } else {
                    task.setStatus(Status.DONE);
                }
                DeadlineTaskDB.update(task);
            }
        }
    }

    // Tạo danh sách 7 ngày trong tuần
    private List<DayInfo> buildWeekDays(LocalDate monday) {
        List<DayInfo> days = new ArrayList<>();
        String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN"};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            String dateString = date.format(formatter);
            days.add(new DayInfo(dayNames[i], dateString, date));
        }
        
        return days;
    }
}
