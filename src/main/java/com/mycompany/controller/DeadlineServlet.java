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

/**
 * Servlet quản lý Deadline Tasks.
 * URL: /deadline
 * Hiển thị tasks theo tuần, hỗ trợ CRUD và filter/sort.
 */
@WebServlet(urlPatterns = {"/deadline"})
public class DeadlineServlet extends HttpServlet {

    /**
     * Xử lý GET request - Hiển thị danh sách tasks theo tuần.
     * Hỗ trợ chuyển tuần, filter theo status, sort theo date/priority.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
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
        
        // Xử lý action chuyển tuần
        String action = request.getParameter("action");
        if ("prevWeek".equals(action)) {
            weekOffset--;
            session.setAttribute("weekOffset", weekOffset);
        } else if ("nextWeek".equals(action)) {
            weekOffset++;
            session.setAttribute("weekOffset", weekOffset);
        }
        
        // Lấy filter và sort params (ưu tiên request > session > default)
        String filter = request.getParameter("filter");
        String sort = request.getParameter("sort");
        
        if (filter != null) {
            session.setAttribute("deadlineFilter", filter);
        } else {
            filter = (String) session.getAttribute("deadlineFilter");
            if (filter == null) filter = "all";
        }
        
        if (sort != null) {
            session.setAttribute("deadlineSort", sort);
        } else {
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
        
        // Sử dụng method kết hợp cả filter và sort
        List<DeadlineTask> tasks = DeadlineTaskDB.getTasksWithFilterAndSort(user, startDateTime, endDateTime, filter, sort);
        
        // Cập nhật status LATE cho các task quá hạn
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
        request.setAttribute("todayStr", realToday.toString());
        
        setCurrentDate(request);
        getServletContext().getRequestDispatcher("/deadline.jsp").forward(request, response);
    }

    /**
     * Xử lý POST request - CRUD operations cho tasks.
     * Actions: add, edit, delete, toggleStatus
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
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
        
        response.sendRedirect(request.getContextPath() + "/deadline");
    }

    /**
     * Xử lý thêm task mới.
     * Không cho phép thêm task vào ngày quá khứ.
     */
    private void handleAddTask(HttpServletRequest request, User user) {
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String dueDateStr = request.getParameter("dueDate");
        String priorityStr = request.getParameter("priority");
        
        if (title != null && !title.trim().isEmpty() && dueDateStr != null) {
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            
            // Không cho thêm task vào quá khứ
            if (dueDate.isBefore(LocalDate.now())) {
                return;
            }
            
            LocalDateTime dueDateTime = dueDate.atTime(23, 59);
            Priority priority = Priority.valueOf(priorityStr);
            
            DeadlineTask task = new DeadlineTask(title.trim(), description, dueDateTime, priority, user);
            DeadlineTaskDB.insert(task);
        }
    }

    /**
     * Xử lý chỉnh sửa task.
     * Không cho sửa task LATE hoặc sửa sang ngày quá khứ.
     */
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

    /**
     * Xử lý xóa task.
     * Chỉ cho xóa task của chính user và không phải LATE.
     */
    private void handleDeleteTask(HttpServletRequest request, User user) {
        String taskIdStr = request.getParameter("taskId");
        
        if (taskIdStr != null) {
            Long taskId = Long.parseLong(taskIdStr);
            DeadlineTask task = DeadlineTaskDB.findById(taskId);
            
            if (task != null && task.getUser().getId().equals(user.getId()) && task.getStatus() != Status.LATE) {
                DeadlineTaskDB.delete(taskId);
            }
        }
    }

    /**
     * Xử lý toggle trạng thái DONE <-> IN_PROGRESS.
     * Không cho toggle task của ngày quá khứ (LATE).
     */
    private void handleToggleStatus(HttpServletRequest request) {
        String taskIdStr = request.getParameter("taskId");
        
        if (taskIdStr != null) {
            Long taskId = Long.parseLong(taskIdStr);
            DeadlineTask task = DeadlineTaskDB.findById(taskId);
            
            if (task != null) {
                LocalDate taskDate = task.getDueDate().toLocalDate();
                if (taskDate.isBefore(LocalDate.now())) {
                    return; // Không toggle task quá hạn
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

    /**
     * Tạo danh sách 7 ngày trong tuần (Mon-Sun).
     * 
     * @param monday Ngày thứ Hai của tuần
     * @return Danh sách DayInfo cho 7 ngày
     */
    private List<DayInfo> buildWeekDays(LocalDate monday) {
        List<DayInfo> days = new ArrayList<>();
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            String dateString = date.format(formatter);
            days.add(new DayInfo(dayNames[i], dateString, date));
        }
        
        return days;
    }
    
    /**
     * Set ngày hiện tại cho header.
     */
    private void setCurrentDate(HttpServletRequest request) {
        String[] dayNames = {"", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        LocalDate today = LocalDate.now();
        String dayName = dayNames[today.getDayOfWeek().getValue()];
        String formattedDate = dayName + ", " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        request.setAttribute("currentDateFormatted", formattedDate);
    }
}
