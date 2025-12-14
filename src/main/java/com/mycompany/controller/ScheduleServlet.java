package com.mycompany.controller;

import com.mycompany.data.ScheduleEventDB;
import com.mycompany.model.FrequencyType;
import com.mycompany.model.RecurrenceRule;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(urlPatterns = {"/schedule"})
public class ScheduleServlet extends HttpServlet {

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
        
        // Tạo danh sách 7 ngày trong tuần
        List<Map<String, Object>> weekDays = buildWeekDays(monday);
        LocalDate sunday = monday.plusDays(6);
        
        // Query events từ database
        LocalDateTime startDateTime = monday.atStartOfDay();
        LocalDateTime endDateTime = sunday.plusDays(1).atStartOfDay();
        
        // Lấy events không lặp
        List<ScheduleEvent> normalEvents = ScheduleEventDB.getEventsByUserAndDateRange(user, startDateTime, endDateTime);
        
        // Lấy recurring events và expand ra các ngày trong tuần
        List<ScheduleEvent> recurringEvents = ScheduleEventDB.getRecurringEventsInRange(user, monday, sunday);
        List<ScheduleEvent> expandedEvents = expandRecurringEvents(recurringEvents, monday, sunday);
        
        // Gộp tất cả events
        List<ScheduleEvent> allEvents = new ArrayList<>();
        allEvents.addAll(normalEvents);
        allEvents.addAll(expandedEvents);
        
        // Gán events vào đúng ngày
        Map<LocalDate, List<ScheduleEvent>> eventsByDate = allEvents.stream()
                .collect(Collectors.groupingBy(e -> e.getStartTime().toLocalDate()));
        
        for (Map<String, Object> day : weekDays) {
            LocalDate date = (LocalDate) day.get("date");
            List<ScheduleEvent> dayEvents = eventsByDate.get(date);
            if (dayEvents != null) {
                // Sort theo startTime
                dayEvents.sort((e1, e2) -> e1.getStartTime().compareTo(e2.getStartTime()));
                day.put("events", dayEvents);
            }
        }
        
        // Lấy recurring events cho dropdown ngoại lệ
        List<ScheduleEvent> recurringForDropdown = ScheduleEventDB.getRecurringEventsByUser(user);
        request.setAttribute("recurringEvents", recurringForDropdown);
        
        // Set attributes cho JSP
        request.setAttribute("weekDays", weekDays);
        request.setAttribute("today", today);
        request.setAttribute("currentMonth", viewDate.getMonthValue());
        request.setAttribute("currentYear", viewDate.getYear());
        
        getServletContext().getRequestDispatcher("/schedule.jsp").forward(request, response);
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
                handleAddEvent(request, user);
                break;
            case "edit":
                handleEditEvent(request, user);
                break;
            case "delete":
                handleDeleteEvent(request, user);
                break;
            case "addException":
                handleAddException(request, user);
                break;
            default:
                break;
        }
        
        // Redirect về GET
        response.sendRedirect(request.getContextPath() + "/schedule");
    }

    // Build list of 7 days in week
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
            day.put("events", new ArrayList<>()); // Danh sách events của ngày
            days.add(day);
        }
        
        return days;
    }

    // Xử lý thêm event mới
    private void handleAddEvent(HttpServletRequest request, User user) {
        String title = request.getParameter("title");
        String eventDateStr = request.getParameter("eventDate");
        String startTimeStr = request.getParameter("startTime");
        String endTimeStr = request.getParameter("endTime");
        String frequencyStr = request.getParameter("frequency");
        String description = request.getParameter("description");
        
        if (title == null || title.trim().isEmpty() || eventDateStr == null || eventDateStr.isEmpty() ||
            startTimeStr == null || endTimeStr == null) {
            return;
        }
        
        try {
            LocalDate eventDate = LocalDate.parse(eventDateStr.trim());
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);
            
            // Validate năm hợp lệ (1900-2100)
            if (eventDate.getYear() < 1900 || eventDate.getYear() > 2100) {
                return;
            }
            
            LocalDateTime startDateTime = eventDate.atTime(startTime);
            LocalDateTime endDateTime = eventDate.atTime(endTime);
            
            ScheduleEvent event = new ScheduleEvent(title.trim(), description, startDateTime, endDateTime, user);
            
            // Xử lý recurring
            if (frequencyStr != null && !"NONE".equals(frequencyStr)) {
                FrequencyType frequency = FrequencyType.valueOf(frequencyStr);
                String untilDateStr = request.getParameter("untilDate");
                LocalDate untilDate = null;
                if (untilDateStr != null && !untilDateStr.isEmpty()) {
                    untilDate = LocalDate.parse(untilDateStr);
                    if (untilDate.getYear() < 1900 || untilDate.getYear() > 2100) {
                        return;
                    }
                }
                
                RecurrenceRule rule = new RecurrenceRule(frequency, untilDate);
                
                // Xử lý byDays cho WEEKLY
                if (frequency == FrequencyType.WEEKLY) {
                    String[] byDaysArr = request.getParameterValues("byDays");
                    if (byDaysArr != null) {
                        List<DayOfWeek> byDays = Arrays.stream(byDaysArr)
                                .map(DayOfWeek::valueOf)
                                .collect(Collectors.toList());
                        rule.setByDays(byDays);
                    }
                }
                
                event.setRecurrenceRule(rule);
            }
            
            ScheduleEventDB.insert(event);
        } catch (Exception e) {
            // Bỏ qua nếu parse lỗi hoặc dữ liệu không hợp lệ
            System.out.println("handleAddEvent error: " + e.getMessage());
        }
    }

    // Xử lý sửa event
    private void handleEditEvent(HttpServletRequest request, User user) {
        String eventIdStr = request.getParameter("eventId");
        if (eventIdStr == null || eventIdStr.isEmpty()) return;
        
        Long eventId = Long.parseLong(eventIdStr);
        ScheduleEvent event = ScheduleEventDB.findById(eventId);
        
        if (event == null || !event.getUser().getId().equals(user.getId())) return;
        
        String title = request.getParameter("title");
        String eventDateStr = request.getParameter("eventDate");
        String startTimeStr = request.getParameter("startTime");
        String endTimeStr = request.getParameter("endTime");
        String description = request.getParameter("description");
        
        if (title != null && !title.trim().isEmpty()) {
            event.setTitle(title.trim());
        }
        if (description != null) {
            event.setDescription(description);
        }
        if (eventDateStr != null && startTimeStr != null && endTimeStr != null) {
            LocalDate eventDate = LocalDate.parse(eventDateStr);
            LocalTime startTime = LocalTime.parse(startTimeStr);
            LocalTime endTime = LocalTime.parse(endTimeStr);
            event.setStartTime(eventDate.atTime(startTime));
            event.setEndTime(eventDate.atTime(endTime));
        }
        
        ScheduleEventDB.update(event);
    }

    // Xử lý xóa event
    private void handleDeleteEvent(HttpServletRequest request, User user) {
        String eventIdStr = request.getParameter("eventId");
        if (eventIdStr == null || eventIdStr.isEmpty()) return;
        
        Long eventId = Long.parseLong(eventIdStr);
        ScheduleEvent event = ScheduleEventDB.findById(eventId);
        
        if (event != null && event.getUser().getId().equals(user.getId())) {
            ScheduleEventDB.delete(eventId);
        }
    }

    // Xử lý thêm ngoại lệ
    private void handleAddException(HttpServletRequest request, User user) {
        String eventIdStr = request.getParameter("eventId");
        String exceptionDateStr = request.getParameter("exceptionDate");
        String exceptionType = request.getParameter("exceptionType");
        
        if (eventIdStr == null || exceptionDateStr == null) return;
        
        Long eventId = Long.parseLong(eventIdStr);
        LocalDate exceptionDate = LocalDate.parse(exceptionDateStr);
        
        ScheduleEvent event = ScheduleEventDB.findById(eventId);
        if (event == null || !event.getUser().getId().equals(user.getId())) return;
        
        String description = request.getParameter("exceptionDescription");
        
        if ("skip".equals(exceptionType)) {
            // Bỏ qua ngày này
            ScheduleEventDB.addExcludedDate(eventId, exceptionDate);
        } else if ("add".equals(exceptionType)) {
            // Thêm ngày này (tạo instance mới với giờ gốc)
            LocalTime originalStartTime = event.getStartTime().toLocalTime();
            LocalTime originalEndTime = event.getEndTime().toLocalTime();
            ScheduleEventDB.createModifiedInstance(eventId, exceptionDate,
                    exceptionDate.atTime(originalStartTime), exceptionDate.atTime(originalEndTime));
        } else if ("modify".equals(exceptionType)) {
            // Thay đổi thời gian
            String newStartTimeStr = request.getParameter("newStartTime");
            String newEndTimeStr = request.getParameter("newEndTime");
            
            if (newStartTimeStr != null && !newStartTimeStr.isEmpty() 
                && newEndTimeStr != null && !newEndTimeStr.isEmpty()) {
                LocalTime newStartTime = LocalTime.parse(newStartTimeStr);
                LocalTime newEndTime = LocalTime.parse(newEndTimeStr);
                
                ScheduleEventDB.createModifiedInstance(eventId, exceptionDate,
                        exceptionDate.atTime(newStartTime), exceptionDate.atTime(newEndTime));
            }
        }
    }

    // Expand recurring events thành các instances trong khoảng thời gian
    private List<ScheduleEvent> expandRecurringEvents(List<ScheduleEvent> recurringEvents, 
            LocalDate startDate, LocalDate endDate) {
        List<ScheduleEvent> expanded = new ArrayList<>();
        
        for (ScheduleEvent event : recurringEvents) {
            RecurrenceRule rule = event.getRecurrenceRule();
            if (rule == null) continue;
            
            LocalDate eventStartDate = event.getStartTime().toLocalDate();
            LocalTime eventStartTime = event.getStartTime().toLocalTime();
            LocalTime eventEndTime = event.getEndTime().toLocalTime();
            
            LocalDate current = eventStartDate;
            LocalDate until = rule.getUntilDate() != null ? rule.getUntilDate() : endDate;
            
            while (!current.isAfter(until) && !current.isAfter(endDate)) {
                if (!current.isBefore(startDate) && !current.isBefore(eventStartDate)) {
                    // Kiểm tra ngày có bị excluded không
                    if (!rule.getExcludedDates().contains(current)) {
                        // Kiểm tra byDays cho WEEKLY
                        boolean shouldInclude = true;
                        if (rule.getFrequency() == FrequencyType.WEEKLY && !rule.getByDays().isEmpty()) {
                            shouldInclude = rule.getByDays().contains(current.getDayOfWeek());
                        }
                        
                        if (shouldInclude) {
                            // Tạo virtual instance
                            ScheduleEvent instance = new ScheduleEvent(
                                event.getTitle(),
                                event.getDescription(),
                                current.atTime(eventStartTime),
                                current.atTime(eventEndTime),
                                event.getUser()
                            );
                            instance.setId(event.getId()); // Giữ ID gốc để reference
                            expanded.add(instance);
                        }
                    }
                }
                
                // Tăng ngày theo frequency
                switch (rule.getFrequency()) {
                    case DAILY:
                        current = current.plusDays(1);
                        break;
                    case WEEKLY:
                        if (rule.getByDays().isEmpty()) {
                            current = current.plusWeeks(1);
                        } else {
                            current = current.plusDays(1);
                        }
                        break;
                    case MONTHLY:
                        current = current.plusMonths(1);
                        break;
                    case YEARLY:
                        current = current.plusYears(1);
                        break;
                }
            }
        }
        
        return expanded;
    }
}
