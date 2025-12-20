package com.mycompany.controller;

import com.mycompany.data.DeadlineTaskDB;
import com.mycompany.data.ScheduleEventDB;
import com.mycompany.data.UserDB;
import com.mycompany.model.DeadlineTask;
import com.mycompany.model.ScheduleEvent;
import com.mycompany.model.User;
import com.mycompany.service.EmailService;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet API gửi email nhắc nhở hàng ngày.
 * URL: /api/daily-reminder
 * Được gọi bởi cron job mỗi giờ để gửi reminder cho users.
 */
@WebServlet(urlPatterns = {"/api/daily-reminder"})
public class DailyReminderServlet extends HttpServlet {

    /**
     * Xử lý GET request - Gửi daily reminder.
     * Lấy users có notification_hour = giờ hiện tại và gửi email.
     * Response: JSON với số lượng sent/failed.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // Lấy giờ hiện tại theo timezone Vietnam
        int currentHour = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).getHour();
        
        // Lấy users có bật notification cho giờ này
        List<User> users = UserDB.getUsersWithNotificationAt(currentHour);
        
        int sentCount = 0;
        int failCount = 0;
        
        for (User user : users) {
            try {
                String emailContent = buildDailyReminderEmail(user);
                if (emailContent != null) {
                    boolean sent = EmailService.sendDailyReminder(user.getEmail(), emailContent);
                    if (sent) {
                        sentCount++;
                    } else {
                        failCount++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                failCount++;
            }
        }
        
        out.print("{\"success\": true, \"sent\": " + sentCount + ", \"failed\": " + failCount + ", \"hour\": " + currentHour + "}");
    }
    
    /**
     * Tạo nội dung HTML cho email daily reminder.
     * Bao gồm danh sách deadlines và schedules của ngày hôm nay.
     * 
     * @param user User cần gửi reminder
     * @return HTML content, null nếu không có tasks/events
     */
    private String buildDailyReminderEmail(User user) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        
        // Lấy deadlines hôm nay
        List<DeadlineTask> deadlines = DeadlineTaskDB.getTasksByUserAndDateRange(user, startOfDay, endOfDay);
        
        // Lấy schedules hôm nay
        List<ScheduleEvent> schedules = ScheduleEventDB.getEventsByUserAndDateRange(user, startOfDay, endOfDay);
        
        // Nếu không có gì thì không gửi
        if (deadlines.isEmpty() && schedules.isEmpty()) {
            return null;
        }
        
        StringBuilder html = new StringBuilder();
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>");
        html.append("<h2 style='color: #6a11cb;'>Daily Reminder - ").append(today).append("</h2>");
        html.append("<p>Hello! Here's your schedule for today:</p>");
        
        // Deadlines section
        if (!deadlines.isEmpty()) {
            html.append("<h3 style='color: #e74c3c; margin-top: 20px;'>Deadlines (").append(deadlines.size()).append(")</h3>");
            html.append("<ul style='padding-left: 20px;'>");
            for (DeadlineTask task : deadlines) {
                html.append("<li style='margin-bottom: 8px;'>");
                html.append("<strong>").append(escapeHtml(task.getTitle())).append("</strong>");
                if (task.getDueDate() != null) {
                    html.append(" - Due: ").append(task.getDueDate().toLocalTime());
                }
                html.append("</li>");
            }
            html.append("</ul>");
        }
        
        // Schedules section
        if (!schedules.isEmpty()) {
            html.append("<h3 style='color: #3498db; margin-top: 20px;'>Schedule (").append(schedules.size()).append(")</h3>");
            html.append("<ul style='padding-left: 20px;'>");
            for (ScheduleEvent event : schedules) {
                html.append("<li style='margin-bottom: 8px;'>");
                html.append("<strong>").append(escapeHtml(event.getTitle())).append("</strong>");
                if (event.getStartTime() != null) {
                    html.append(" - ").append(event.getStartTime().toLocalTime());
                    if (event.getEndTime() != null) {
                        html.append(" to ").append(event.getEndTime().toLocalTime());
                    }
                }
                html.append("</li>");
            }
            html.append("</ul>");
        }
        
        html.append("<hr style='margin: 30px 0; border: none; border-top: 1px solid #eee;'>");
        html.append("<p style='color: #999; font-size: 12px;'>TodoList App - Daily Reminder</p>");
        html.append("</div>");
        
        return html.toString();
    }
    
    /**
     * Escape các ký tự HTML đặc biệt để tránh XSS.
     * 
     * @param text Text cần escape
     * @return Text đã được escape
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
