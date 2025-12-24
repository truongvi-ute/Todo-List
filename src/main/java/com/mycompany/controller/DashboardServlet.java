package com.mycompany.controller;

import com.mycompany.data.DayEventDB;
import com.mycompany.data.DeadlineTaskDB;
import com.mycompany.model.DayEvent;
import com.mycompany.model.DeadlineTask;
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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet hiển thị trang Dashboard (trang chủ sau đăng nhập).
 * URL: /dashboard
 * Hiển thị: Calendar, tasks và events của ngày được chọn.
 */
@WebServlet(urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    /**
     * Xử lý GET request - Hiển thị dashboard.
     * Lấy tasks và events cho ngày được chọn, tạo calendar data.
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
        LocalDate today = LocalDate.now();
        
        // Lấy ngày được chọn (ưu tiên: param > session > today)
        LocalDate selectedDate = today;
        String dateParam = request.getParameter("date");
        if (dateParam != null && !dateParam.isEmpty()) {
            selectedDate = LocalDate.parse(dateParam);
            session.setAttribute("dashboardSelectedDate", selectedDate.toString());
        } else {
            String savedDate = (String) session.getAttribute("dashboardSelectedDate");
            if (savedDate != null) {
                selectedDate = LocalDate.parse(savedDate);
            }
        }
        
        // Lấy month offset cho calendar (để chuyển tháng)
        int monthOffset = 0;
        String monthOffsetParam = request.getParameter("monthOffset");
        if (monthOffsetParam != null && !monthOffsetParam.isEmpty()) {
            monthOffset = Integer.parseInt(monthOffsetParam);
            session.setAttribute("dashboardMonthOffset", monthOffset);
        } else {
            Integer savedOffset = (Integer) session.getAttribute("dashboardMonthOffset");
            if (savedOffset != null) {
                monthOffset = savedOffset;
            }
        }
        
        // Lấy sort preference từ session
        String sort = (String) session.getAttribute("deadlineSort");
        if (sort == null) sort = "date";
        
        // Query deadline tasks cho ngày được chọn
        LocalDateTime startOfDay = selectedDate.atStartOfDay();
        LocalDateTime endOfDay = selectedDate.plusDays(1).atStartOfDay();
        
        List<DeadlineTask> deadlineTasks = DeadlineTaskDB.getTasksByUserAndDate(user, startOfDay, endOfDay, sort);
        
        // Cập nhật status LATE cho tasks quá hạn
        for (DeadlineTask task : deadlineTasks) {
            if (task.getStatus() != Status.DONE && task.getDueDate().toLocalDate().isBefore(today)) {
                task.setStatus(Status.LATE);
                DeadlineTaskDB.update(task);
            }
        }
        
        // Query DayEvents cho ngày được chọn (đã bao gồm cả recurring)
        List<DayEvent> dayEvents = DayEventDB.getByUserAndDate(user, selectedDate);
        
        // Tạo calendar data (42 ngày)
        YearMonth viewMonth = YearMonth.from(selectedDate).plusMonths(monthOffset);
        List<LocalDate> calendarDays = buildCalendarDays(viewMonth);
        
        // Set attributes cho JSP
        request.setAttribute("today", today);
        request.setAttribute("selectedDate", selectedDate);
        request.setAttribute("deadlineTasks", deadlineTasks);
        request.setAttribute("dayEvents", dayEvents);
        request.setAttribute("viewMonth", viewMonth);
        request.setAttribute("viewMonthValue", viewMonth.getMonthValue());
        request.setAttribute("viewYear", viewMonth.getYear());
        request.setAttribute("calendarDays", calendarDays);
        request.setAttribute("monthOffset", monthOffset);
        
        setCurrentDate(request);
        getServletContext().getRequestDispatcher("/home.jsp").forward(request, response);
    }

    /**
     * Xử lý POST request - Toggle trạng thái task.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/signin.jsp");
            return;
        }
        
        String action = request.getParameter("action");
        String dateParam = request.getParameter("date");
        
        if ("toggleTask".equals(action)) {
            String taskIdStr = request.getParameter("taskId");
            if (taskIdStr != null) {
                Long taskId = Long.parseLong(taskIdStr);
                DeadlineTask task = DeadlineTaskDB.findById(taskId);
                
                // Toggle status DONE <-> IN_PROGRESS (không cho toggle task LATE)
                if (task != null && task.getStatus() != Status.LATE) {
                    if (task.getStatus() == Status.DONE) {
                        task.setStatus(Status.IN_PROGRESS);
                    } else {
                        task.setStatus(Status.DONE);
                    }
                    DeadlineTaskDB.update(task);
                }
            }
        }
        
        // Redirect về GET với ngày đã chọn
        String redirectUrl = request.getContextPath() + "/dashboard";
        if (dateParam != null && !dateParam.isEmpty()) {
            redirectUrl += "?date=" + dateParam;
        }
        response.sendRedirect(redirectUrl);
    }

    /**
     * Tạo danh sách 42 ngày cho calendar (6 tuần).
     * Bắt đầu từ thứ Hai của tuần chứa ngày 1 của tháng.
     * 
     * @param viewMonth Tháng cần hiển thị
     * @return Danh sách 42 LocalDate
     */
    private List<LocalDate> buildCalendarDays(YearMonth viewMonth) {
        List<LocalDate> days = new ArrayList<>();
        
        LocalDate firstOfMonth = viewMonth.atDay(1);
        LocalDate calendarStart = firstOfMonth.with(DayOfWeek.MONDAY);
        if (calendarStart.isAfter(firstOfMonth)) {
            calendarStart = calendarStart.minusWeeks(1);
        }
        
        for (int i = 0; i < 42; i++) {
            days.add(calendarStart.plusDays(i));
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
        String formattedDate = dayName + ", " + today.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        request.setAttribute("currentDateFormatted", formattedDate);
    }
}
