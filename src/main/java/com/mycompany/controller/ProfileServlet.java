package com.mycompany.controller;

import com.mycompany.data.UserDB;
import com.mycompany.model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("loginedUser") == null) {
            response.sendRedirect("login");
            return;
        }
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }

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
            handleChangePassword(request, user);
        } else if ("updateNotification".equals(action)) {
            handleUpdateNotification(request, user, session);
        }
        
        request.getRequestDispatcher("/profile.jsp").forward(request, response);
    }
    
    private void handleChangePassword(HttpServletRequest request, User user) {
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        if (!user.getPassword().equals(currentPassword)) {
            request.setAttribute("message", "Current password is incorrect!");
        } else if (newPassword.length() < 6) {
            request.setAttribute("message", "New password must be at least 6 characters!");
        } else if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("message", "New passwords do not match!");
        } else {
            UserDB.updatePassword(user.getEmail(), newPassword);
            user.setPassword(newPassword);
            request.setAttribute("message", "Password updated successfully!");
        }
    }
    
    private void handleUpdateNotification(HttpServletRequest request, User user, HttpSession session) {
        boolean enabled = request.getParameter("notificationEnabled") != null;
        int hour = Integer.parseInt(request.getParameter("notificationHour"));
        
        UserDB.updateNotificationSettings(user.getEmail(), enabled, hour);
        user.setNotificationEnabled(enabled);
        user.setNotificationHour(hour);
        session.setAttribute("loginedUser", user);
        
        request.setAttribute("message", "Notification settings saved successfully!");
    }
}
