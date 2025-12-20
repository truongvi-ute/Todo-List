package com.mycompany.controller;

import com.mycompany.data.DeadlineTaskDB;
import com.mycompany.data.ScheduleEventDB;
import com.mycompany.model.DeadlineTask;
import com.mycompany.model.FrequencyType;
import com.mycompany.model.RecurrenceRule;
import com.mycompany.model.ScheduleEvent;
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
        
        // Query schedule events cho ngày được chọn (bao gồm recurring)
        List<ScheduleEvent> scheduleEvents = getEventsForDate(user, selectedDate);
        
        // Tạo calendar data (42 ngày)
        YearMonth viewMonth = YearMonth.from(selectedDate).plusMonths(monthOffset);
        List<LocalDate> calendarDays = buildCalendarDays(viewMonth);
        
        // Set attributes cho JSP
        request.setAttribute("today", today);
        request.setAttribute("selectedDate", selectedDate);
        request.setAttribute("deadlineTasks", deadlineTasks);
        request.setAttribute("scheduleEvents", scheduleEvents);
        request.setAttribute("viewMonth", viewMonth);
        request.setAttribute("viewMonthValue", viewMonth.getMonthValue());
        request.setAttribute("viewYear", viewMonth.getYear());
        request.setAttribute("calendarDays", calendarDays);
        request.setAttribute("monthOffset", monthOffset);
        
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
     * Lấy tất cả events cho ngày cụ thể (bao gồm recurring events).
     * 
     * @param user User sở hữu events
     * @param date Ngày cần lấy events
     * @return Danh sách events đã sắp xếp theo startTime
     */
    private List<ScheduleEvent> getEventsForDate(User user, LocalDate date) {
        List<ScheduleEvent> result = new ArrayList<>();
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        
        // Lấy events không lặp
        List<ScheduleEvent> normalEvents = ScheduleEventDB.getEventsByUserAndDateRange(user, startOfDay, endOfDay);
        result.addAll(normalEvents);
        
        // Lấy recurring events và check xem có xuất hiện trong ngày này không
        List<ScheduleEvent> recurringEvents = ScheduleEventDB.getRecurringEventsInRange(user, date, date);
        for (ScheduleEvent event : recurringEvents) {
            if (isEventOnDate(event, date)) {
                // Tạo virtual instance cho ngày này
                ScheduleEvent instance = new ScheduleEvent(
                    event.getTitle(),
                    event.getDescription(),
                    date.atTime(event.getStartTime().toLocalTime()),
                    date.atTime(event.getEndTime().toLocalTime()),
                    event.getUser()
                );
                instance.setId(event.getId());
                result.add(instance);
            }
        }
        
        // Sort theo startTime
        result.sort((e1, e2) -> e1.getStartTime().compareTo(e2.getStartTime()));
        
        return result;
    }

    /**
     * Kiểm tra recurring event có xuất hiện trong ngày cụ thể không.
     * Xét các yếu tố: frequency, byDays, excludedDates, untilDate.
     * 
     * @param event Recurring event cần kiểm tra
     * @param date Ngày cần kiểm tra
     * @return true nếu event xuất hiện trong ngày này
     */
    private boolean isEventOnDate(ScheduleEvent event, LocalDate date) {
        RecurrenceRule rule = event.getRecurrenceRule();
        if (rule == null) return false;
        
        LocalDate eventStartDate = event.getStartTime().toLocalDate();
        
        // Ngày phải >= ngày bắt đầu event
        if (date.isBefore(eventStartDate)) return false;
        
        // Ngày phải <= untilDate (nếu có)
        if (rule.getUntilDate() != null && date.isAfter(rule.getUntilDate())) return false;
        
        // Kiểm tra ngày có bị excluded không
        if (rule.getExcludedDates().contains(date)) return false;
        
        // Kiểm tra theo frequency
        switch (rule.getFrequency()) {
            case DAILY:
                return true;
            case WEEKLY:
                if (!rule.getByDays().isEmpty()) {
                    return rule.getByDays().contains(date.getDayOfWeek());
                }
                return date.getDayOfWeek() == eventStartDate.getDayOfWeek();
            case MONTHLY:
                return date.getDayOfMonth() == eventStartDate.getDayOfMonth();
            case YEARLY:
                return date.getDayOfMonth() == eventStartDate.getDayOfMonth() 
                    && date.getMonthValue() == eventStartDate.getMonthValue();
            default:
                return false;
        }
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
}
