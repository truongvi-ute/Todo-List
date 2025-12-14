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

@WebServlet(urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

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
        
        // Lấy ngày được chọn (ưu tiên param > session > today)
        LocalDate selectedDate = today;
        String dateParam = request.getParameter("date");
        if (dateParam != null && !dateParam.isEmpty()) {
            selectedDate = LocalDate.parse(dateParam);
            session.setAttribute("dashboardSelectedDate", selectedDate.toString());
        } else {
            // Lấy từ session nếu không có param
            String savedDate = (String) session.getAttribute("dashboardSelectedDate");
            if (savedDate != null) {
                selectedDate = LocalDate.parse(savedDate);
            }
        }
        
        // Lấy month offset cho calendar (ưu tiên param > session > 0)
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
        
        // Lấy sort preference từ session (đã lưu từ trang deadline)
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
        
        // Query schedule events cho ngày được chọn
        List<ScheduleEvent> scheduleEvents = getEventsForDate(user, selectedDate);
        
        // Tạo calendar data
        YearMonth viewMonth = YearMonth.from(selectedDate).plusMonths(monthOffset);
        List<LocalDate> calendarDays = buildCalendarDays(viewMonth);
        
        // Set attributes
        request.setAttribute("today", today);
        request.setAttribute("selectedDate", selectedDate);
        request.setAttribute("deadlineTasks", deadlineTasks);
        request.setAttribute("scheduleEvents", scheduleEvents);
        request.setAttribute("viewMonth", viewMonth);
        request.setAttribute("viewMonthValue", viewMonth.getMonthValue());
        request.setAttribute("viewYear", viewMonth.getYear());
        request.setAttribute("calendarDays", calendarDays);
        request.setAttribute("monthOffset", monthOffset);
        
        // Debug log
        System.out.println("Dashboard: selectedDate=" + selectedDate + ", tasks=" + deadlineTasks.size() + ", events=" + scheduleEvents.size());
        
        getServletContext().getRequestDispatcher("/home.jsp").forward(request, response);
    }

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

    // Lấy events cho ngày cụ thể (bao gồm recurring)
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

    // Kiểm tra recurring event có xuất hiện trong ngày không
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

    // Tạo danh sách 42 ngày cho calendar
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
