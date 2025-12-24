package com.mycompany.controller;

import com.mycompany.data.UserDB;
import com.mycompany.model.User;
import com.mycompany.service.PasswordUtil;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet quản lý trang profile người dùng.
 * URL: /profile
 * Chức năng: Đổi mật khẩu, cài đặt thông báo email.
 */
@WebServlet(urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    /**
     * Xử lý GET request - Hiển thị trang profile.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("loginedUser") == null) {
            response.sendRedirect("login");
            return;
        }
        setCurrentDate(request);
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }

    /**
     * Xử lý POST request - Cập nhật thông tin profile.
     * Actions: changePassword, updateNotification
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginedUser");
        
        if (user == null) {
            response.sendRedirect("login");
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("changePassword".equals(action)) {
            handleChangePassword(request, user, session);
        } else if ("updateNotification".equals(action)) {
            handleUpdateNotification(request, user, session);
        }
        
        setCurrentDate(request);
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }
    
    /**
     * Xử lý đổi mật khẩu.
     * Validate password cũ, hash và lưu password mới.
     */
    private void handleChangePassword(HttpServletRequest request, User user, HttpSession session) {
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // Xác thực password hiện tại bằng BCrypt
        if (!PasswordUtil.verify(currentPassword, user.getPassword())) {
            request.setAttribute("message", "Current password is incorrect!");
        } else if (newPassword.length() < 6) {
            request.setAttribute("message", "New password must be at least 6 characters!");
        } else if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("message", "New passwords do not match!");
        } else {
            // Hash password mới và cập nhật
            String hashedPassword = PasswordUtil.hash(newPassword);
            UserDB.updatePassword(user.getEmail(), hashedPassword);
            user.setPassword(hashedPassword);
            session.setAttribute("loginedUser", user);
            request.setAttribute("message", "Password updated successfully!");
        }
    }
    
    /**
     * Xử lý cập nhật cài đặt thông báo email hàng ngày.
     */
    private void handleUpdateNotification(HttpServletRequest request, User user, HttpSession session) {
        boolean enabled = request.getParameter("notificationEnabled") != null;
        int hour = Integer.parseInt(request.getParameter("notificationHour"));
        
        UserDB.updateNotificationSettings(user.getEmail(), enabled, hour);
        user.setNotificationEnabled(enabled);
        user.setNotificationHour(hour);
        session.setAttribute("loginedUser", user);
        
        request.setAttribute("message", "Notification settings saved successfully!");
    }
    
    /**
     * Set ngày hiện tại cho header.
     */
    private void setCurrentDate(HttpServletRequest request) {
        String[] dayNames = {"", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        java.time.LocalDate today = java.time.LocalDate.now();
        String dayName = dayNames[today.getDayOfWeek().getValue()];
        String formattedDate = dayName + ", " + today.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        request.setAttribute("currentDateFormatted", formattedDate);
    }
}
