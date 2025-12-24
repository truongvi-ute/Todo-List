package com.mycompany.controller;

import com.mycompany.data.DayEventDB;
import com.mycompany.data.ScheduleEventDB;
import com.mycompany.model.DayEvent;
import com.mycompany.model.ScheduleEvent;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servlet quản lý Schedule Events.
 * URL: /schedule
 * Hiển thị events theo tuần, hỗ trợ CRUD cho ScheduleEvent và DayEvent.
 */
@WebServlet(urlPatterns = {"/schedule"})
public class ScheduleServlet extends HttpServlet {

    /**
     * Xử lý GET request - Hiển thị lịch theo tuần.
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
        
        // Lấy week offset từ session
        Integer weekOffset = (Integer) session.getAttribute("scheduleWeekOffset");
        if (weekOffset == null) {
            weekOffset = 0;
        }
        
        // Xử lý action chuyển tuần
        String action = request.getParameter("action");
        if ("prevWeek".equals(action)) {
            weekOffset--;
            session.setAttribute("scheduleWeekOffset", weekOffset);
        } else if ("nextWeek".equals(action)) {
            weekOffset++;
            session.setAttribute("scheduleWeekOffset", weekOffset);
        }
        
        // Tính toán tuần hiện tại
        LocalDate today = LocalDate.now();
        LocalDate viewDate = today.plusWeeks(weekOffset);
        LocalDate monday = viewDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        
        // Tạo danh sách 7 ngày trong tuần
        List<Map<String, Object>> weekDays = buildWeekDays(monday);
        
        // Query DayEvents từ database
        List<DayEvent> allDayEvents = DayEventDB.getByUserAndDateRange(user, monday, sunday);
        
        // Gán events vào đúng ngày
        Map<LocalDate, List<DayEvent>> eventsByDate = allDayEvents.stream()
                .collect(Collectors.groupingBy(DayEvent::getSpecificDate));
        
        for (Map<String, Object> day : weekDays) {
            LocalDate date = (LocalDate) day.get("date");
            List<DayEvent> dayEvents = eventsByDate.get(date);
            if (dayEvents != null) {
                // Sort theo giờ bắt đầu
                dayEvents.sort((e1, e2) -> e1.getEffectiveStartTime().compareTo(e2.getEffectiveStartTime()));
                day.put("events", dayEvents);
            }
        }
        
        // Lấy tất cả ScheduleEvents cho dropdown quản lý
        List<ScheduleEvent> allScheduleEvents = ScheduleEventDB.getAllByUser(user);
        request.setAttribute("scheduleEvents", allScheduleEvents);
        
        // Set attributes cho JSP
        request.setAttribute("weekDays", weekDays);
        request.setAttribute("today", today);
        request.setAttribute("currentMonth", viewDate.getMonthValue());
        request.setAttribute("currentYear", viewDate.getYear());
        
        setCurrentDate(request);
        getServletContext().getRequestDispatcher("/schedule.jsp").forward(request, response);
    }

    /**
     * Xử lý POST request - CRUD operations.
     * Actions: add, edit, delete, cancelDay, restoreDay, overrideTime
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
        
        String errorMessage = null;
        
        switch (action) {
            case "add":
                if (!handleAddEvent(request, user)) {
                    errorMessage = (String) request.getAttribute("errorMessage");
                }
                break;
            case "edit":
                if (!handleEditEvent(request, user)) {
                    errorMessage = (String) request.getAttribute("errorMessage");
                }
                break;
            case "delete":
                handleDeleteEvent(request, user);
                break;
            case "cancelDay":
                handleCancelDay(request, user);
                break;
            case "restoreDay":
                handleRestoreDay(request, user);
                break;
            case "overrideTime":
                if (!handleOverrideTime(request, user)) {
                    errorMessage = (String) request.getAttribute("errorMessage");
                }
                break;
            default:
                break;
        }
        
        // Redirect với error message nếu có
        if (errorMessage != null) {
            response.sendRedirect(request.getContextPath() + "/schedule?error=" + java.net.URLEncoder.encode(errorMessage, "UTF-8"));
        } else {
            response.sendRedirect(request.getContextPath() + "/schedule");
        }
    }

    /**
     * Tạo danh sách 7 ngày trong tuần với metadata.
     */
    private List<Map<String, Object>> buildWeekDays(LocalDate monday) {
        List<Map<String, Object>> days = new ArrayList<>();
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            Map<String, Object> day = new HashMap<>();
            day.put("dayName", dayNames[i]);
            day.put("dateString", date.format(formatter));
            day.put("date", date);
            day.put("events", new ArrayList<>());
            days.add(day);
        }
        
        return days;
    }

    /**
     * Xử lý thêm ScheduleEvent mới.
     * Tự động generate DayEvents dựa trên recurrenceDays.
     */
    private boolean handleAddEvent(HttpServletRequest request, User user) {
        String title = request.getParameter("title");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String startTimeStr = request.getParameter("startTime");
        String endTimeStr = request.getParameter("endTime");
        String recurrenceDays = request.getParameter("recurrenceDays");
        String description = request.getParameter("description");
        
        if (title == null || title.trim().isEmpty() || 
            startDateStr == null || startDateStr.isEmpty() ||
            endDateStr == null || endDateStr.isEmpty() ||
            startTimeStr == null || startTimeStr.isEmpty() ||
            endTimeStr == null || endTimeStr.isEmpty()) {
            request.setAttribute("errorMessage", "Please fill all required fields!");
            return false;
        }
        
        try {
            LocalDate startDate = LocalDate.parse(startDateStr.trim());
            LocalDate endDate = LocalDate.parse(endDateStr.trim());
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);
            
            // Validate ngày
            if (endDate.isBefore(startDate)) {
                request.setAttribute("errorMessage", "End date cannot be before start date!");
                return false;
            }
            
            // Validate khoảng cách không quá 1 năm (365 ngày)
            if (startDate.plusYears(1).isBefore(endDate)) {
                request.setAttribute("errorMessage", "Date range cannot exceed 1 year!");
                return false;
            }
            
            // Validate thời gian trong khoảng 06:00 - 23:59
            LocalTime minTime = LocalTime.of(6, 0);
            LocalTime maxTime = LocalTime.of(23, 59);
            
            if (startTime.isBefore(minTime) || startTime.isAfter(maxTime)) {
                request.setAttribute("errorMessage", "Start time must be between 06:00 and 23:59!");
                return false;
            }
            
            if (endTime.isBefore(minTime) || endTime.isAfter(maxTime)) {
                request.setAttribute("errorMessage", "End time must be between 06:00 and 23:59!");
                return false;
            }
            
            if (!endTime.isAfter(startTime)) {
                request.setAttribute("errorMessage", "End time must be after start time!");
                return false;
            }
            
            // Nếu startDate == endDate (sự kiện 1 ngày), bỏ qua recurrenceDays
            String finalRecurrenceDays = null;
            if (!startDate.equals(endDate) && recurrenceDays != null && !recurrenceDays.trim().isEmpty()) {
                finalRecurrenceDays = recurrenceDays.trim();
            }
            
            // Kiểm tra trùng lịch trước khi thêm
            List<LocalDate> conflictDates = checkOverlappingEvents(user, startDate, endDate, 
                    startTime, endTime, finalRecurrenceDays, null);
            
            if (!conflictDates.isEmpty()) {
                // Tạo thông báo lỗi với các ngày bị trùng
                String conflictDatesStr = conflictDates.stream()
                        .limit(5) // Chỉ hiển thị tối đa 5 ngày
                        .map(d -> d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        .collect(Collectors.joining(", "));
                
                if (conflictDates.size() > 5) {
                    conflictDatesStr += " and " + (conflictDates.size() - 5) + " more";
                }
                
                request.setAttribute("errorMessage", "Time conflict with existing event on: " + conflictDatesStr);
                return false;
            }
            
            // Tạo ScheduleEvent
            ScheduleEvent event = new ScheduleEvent(
                title.trim(), 
                description, 
                user,
                startDate, 
                endDate, 
                finalRecurrenceDays,
                startTime, 
                endTime
            );
            
            // Insert sẽ tự động generate DayEvents
            ScheduleEventDB.insert(event);
            return true;
            
        } catch (Exception e) {
            System.out.println("handleAddEvent error: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Invalid input data!");
            return false;
        }
    }

    /**
     * Xử lý sửa ScheduleEvent.
     */
    private boolean handleEditEvent(HttpServletRequest request, User user) {
        String eventIdStr = request.getParameter("eventId");
        if (eventIdStr == null || eventIdStr.isEmpty()) return false;
        
        Long eventId = Long.parseLong(eventIdStr);
        ScheduleEvent event = ScheduleEventDB.findById(eventId);
        
        // Kiểm tra quyền sở hữu
        if (event == null || !event.getUser().getId().equals(user.getId())) return false;
        
        String title = request.getParameter("title");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String startTimeStr = request.getParameter("startTime");
        String endTimeStr = request.getParameter("endTime");
        String recurrenceDays = request.getParameter("recurrenceDays");
        String description = request.getParameter("description");
        
        boolean needRegenerate = false;
        
        if (title != null && !title.trim().isEmpty()) {
            event.setTitle(title.trim());
        }
        if (description != null) {
            event.setDescription(description);
        }
        
        // Kiểm tra xem có thay đổi cấu trúc không
        if (startDateStr != null && !startDateStr.isEmpty()) {
            LocalDate newStartDate = LocalDate.parse(startDateStr);
            if (!newStartDate.equals(event.getStartDate())) {
                event.setStartDate(newStartDate);
                needRegenerate = true;
            }
        }
        
        if (endDateStr != null && !endDateStr.isEmpty()) {
            LocalDate newEndDate = LocalDate.parse(endDateStr);
            if (!newEndDate.equals(event.getEndDate())) {
                event.setEndDate(newEndDate);
                needRegenerate = true;
            }
        }
        
        if (startTimeStr != null && !startTimeStr.isEmpty()) {
            LocalTime newStartTime = LocalTime.parse(startTimeStr);
            event.setDefaultStartTime(newStartTime);
        }
        
        if (endTimeStr != null && !endTimeStr.isEmpty()) {
            LocalTime newEndTime = LocalTime.parse(endTimeStr);
            event.setDefaultEndTime(newEndTime);
        }
        
        String currentRecurrence = event.getRecurrenceDays();
        String newRecurrence = recurrenceDays != null && !recurrenceDays.trim().isEmpty() ? recurrenceDays.trim() : null;
        if ((currentRecurrence == null && newRecurrence != null) ||
            (currentRecurrence != null && !currentRecurrence.equals(newRecurrence))) {
            event.setRecurrenceDays(newRecurrence);
            needRegenerate = true;
        }
        
        ScheduleEventDB.update(event, needRegenerate);
        return true;
    }

    /**
     * Xử lý xóa ScheduleEvent.
     */
    private void handleDeleteEvent(HttpServletRequest request, User user) {
        String eventIdStr = request.getParameter("eventId");
        if (eventIdStr == null || eventIdStr.isEmpty()) return;
        
        Long eventId = Long.parseLong(eventIdStr);
        ScheduleEvent event = ScheduleEventDB.findById(eventId);
        
        if (event != null && event.getUser().getId().equals(user.getId())) {
            ScheduleEventDB.delete(eventId);
        }
    }

    /**
     * Xử lý hủy một buổi cụ thể (DayEvent).
     */
    private void handleCancelDay(HttpServletRequest request, User user) {
        String dayEventIdStr = request.getParameter("dayEventId");
        if (dayEventIdStr == null || dayEventIdStr.isEmpty()) return;
        
        Long dayEventId = Long.parseLong(dayEventIdStr);
        DayEvent dayEvent = DayEventDB.findById(dayEventId);
        
        // Kiểm tra quyền sở hữu
        if (dayEvent != null && dayEvent.getScheduleEvent().getUser().getId().equals(user.getId())) {
            DayEventDB.cancel(dayEventId);
        }
    }

    /**
     * Xử lý khôi phục một buổi đã hủy.
     */
    private void handleRestoreDay(HttpServletRequest request, User user) {
        String dayEventIdStr = request.getParameter("dayEventId");
        if (dayEventIdStr == null || dayEventIdStr.isEmpty()) return;
        
        Long dayEventId = Long.parseLong(dayEventIdStr);
        DayEvent dayEvent = DayEventDB.findById(dayEventId);
        
        if (dayEvent != null && dayEvent.getScheduleEvent().getUser().getId().equals(user.getId())) {
            DayEventDB.restore(dayEventId);
        }
    }

    /**
     * Xử lý ghi đè giờ cho một buổi cụ thể.
     * Kiểm tra trùng lịch trước khi thay đổi.
     */
    private boolean handleOverrideTime(HttpServletRequest request, User user) {
        String dayEventIdStr = request.getParameter("dayEventId");
        String newStartTimeStr = request.getParameter("newStartTime");
        String newEndTimeStr = request.getParameter("newEndTime");
        
        if (dayEventIdStr == null || dayEventIdStr.isEmpty()) return false;
        
        Long dayEventId = Long.parseLong(dayEventIdStr);
        DayEvent dayEvent = DayEventDB.findById(dayEventId);
        
        if (dayEvent != null && dayEvent.getScheduleEvent().getUser().getId().equals(user.getId())) {
            LocalTime newStartTime = null;
            LocalTime newEndTime = null;
            
            if (newStartTimeStr != null && !newStartTimeStr.isEmpty()) {
                newStartTime = LocalTime.parse(newStartTimeStr);
            }
            if (newEndTimeStr != null && !newEndTimeStr.isEmpty()) {
                newEndTime = LocalTime.parse(newEndTimeStr);
            }
            
            // Nếu không có thời gian mới, dùng thời gian mặc định
            if (newStartTime == null) {
                newStartTime = dayEvent.getScheduleEvent().getDefaultStartTime();
            }
            if (newEndTime == null) {
                newEndTime = dayEvent.getScheduleEvent().getDefaultEndTime();
            }
            
            // Validate thời gian
            if (!newEndTime.isAfter(newStartTime)) {
                request.setAttribute("errorMessage", "End time must be after start time!");
                return false;
            }
            
            // Kiểm tra trùng lịch (loại trừ chính dayEvent này)
            LocalDate eventDate = dayEvent.getSpecificDate();
            if (ScheduleEventDB.hasOverlappingEvent(user, eventDate, newStartTime, newEndTime, dayEventId)) {
                request.setAttribute("errorMessage", "Time conflict with existing event on " + 
                        eventDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                return false;
            }
            
            DayEventDB.overrideTime(dayEventId, 
                    newStartTimeStr != null && !newStartTimeStr.isEmpty() ? LocalTime.parse(newStartTimeStr) : null,
                    newEndTimeStr != null && !newEndTimeStr.isEmpty() ? LocalTime.parse(newEndTimeStr) : null);
            return true;
        }
        return false;
    }
    
    /**
     * Set ngày hiện tại cho header.
     */
    private void setCurrentDate(HttpServletRequest request) {
        String[] dayNames = {"", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        java.time.LocalDate today = java.time.LocalDate.now();
        String dayName = dayNames[today.getDayOfWeek().getValue()];
        String formattedDate = dayName + ", " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        request.setAttribute("currentDateFormatted", formattedDate);
    }
    
    /**
     * Kiểm tra trùng lịch cho tất cả các ngày sẽ được tạo DayEvent.
     * 
     * @param user User sở hữu events
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param startTime Giờ bắt đầu
     * @param endTime Giờ kết thúc
     * @param recurrenceDays Các ngày lặp lại (MON,TUE,...)
     * @param excludeEventId ID của ScheduleEvent cần loại trừ (khi edit)
     * @return Danh sách các ngày bị trùng lịch
     */
    private List<LocalDate> checkOverlappingEvents(User user, LocalDate startDate, LocalDate endDate,
            LocalTime startTime, LocalTime endTime, String recurrenceDays, Long excludeEventId) {
        
        List<LocalDate> conflictDates = new ArrayList<>();
        
        // Parse recurrenceDays thành list DayOfWeek
        List<DayOfWeek> targetDays = parseRecurrenceDays(recurrenceDays);
        
        // Nếu không có recurrenceDays, chỉ kiểm tra startDate
        if (targetDays.isEmpty()) {
            if (ScheduleEventDB.hasOverlappingEvent(user, startDate, startTime, endTime, null)) {
                conflictDates.add(startDate);
            }
            return conflictDates;
        }
        
        // Duyệt từ startDate đến endDate, kiểm tra các ngày khớp
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (targetDays.contains(current.getDayOfWeek())) {
                if (ScheduleEventDB.hasOverlappingEvent(user, current, startTime, endTime, null)) {
                    conflictDates.add(current);
                }
            }
            current = current.plusDays(1);
        }
        
        return conflictDates;
    }
    
    /**
     * Parse chuỗi recurrenceDays thành list DayOfWeek.
     */
    private List<DayOfWeek> parseRecurrenceDays(String recurrenceDays) {
        if (recurrenceDays == null || recurrenceDays.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<DayOfWeek> result = new ArrayList<>();
        
        for (String day : recurrenceDays.split(",")) {
            day = day.trim().toUpperCase();
            if (day.isEmpty()) continue;
            
            switch (day) {
                case "MON": result.add(DayOfWeek.MONDAY); break;
                case "TUE": result.add(DayOfWeek.TUESDAY); break;
                case "WED": result.add(DayOfWeek.WEDNESDAY); break;
                case "THU": result.add(DayOfWeek.THURSDAY); break;
                case "FRI": result.add(DayOfWeek.FRIDAY); break;
                case "SAT": result.add(DayOfWeek.SATURDAY); break;
                case "SUN": result.add(DayOfWeek.SUNDAY); break;
            }
        }
        
        return result;
    }
}
