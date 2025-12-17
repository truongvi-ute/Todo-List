<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.mycompany.model.User"%>
<%
    User user = (User) session.getAttribute("loginedUser");
    if (user == null) {
        response.sendRedirect("login");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TodoList</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile.css">
</head>
<body>
    <%@ include file="header.jsp" %>
    
    <main class="profile-container">
        <% 
            String msg = (String) request.getAttribute("message");
            if (msg != null) {
                String alertClass = msg.toLowerCase().contains("success") ? "alert-success" : "alert-error";
        %>
            <div class="alert <%= alertClass %>"><%= msg %></div>
        <% } %>
        
        <div class="profile-grid">
            <!-- Left: Edit Profile -->
            <div class="profile-card">
                <h2>Edit Profile</h2>
                
                <div class="profile-section">
                    <h3>Account Information</h3>
                    <p class="email-display"><strong>Email:</strong> <%= user.getEmail() %></p>
                </div>
                
                <div class="profile-section">
                    <h3>Change Password</h3>
                    <form action="profile" method="POST" class="profile-form">
                        <input type="hidden" name="action" value="changePassword">
                        
                        <div class="form-group">
                            <label for="currentPassword">Current Password</label>
                            <input type="password" id="currentPassword" name="currentPassword" required>
                        </div>
                        
                        <div class="form-group">
                            <label for="newPassword">New Password</label>
                            <input type="password" id="newPassword" name="newPassword" required minlength="6">
                        </div>
                        
                        <div class="form-group">
                            <label for="confirmPassword">Confirm New Password</label>
                            <input type="password" id="confirmPassword" name="confirmPassword" required>
                        </div>
                        
                        <button type="submit" class="btn-primary">Update Password</button>
                    </form>
                </div>
                
            </div>
            
            <!-- Right: Notification Settings -->
            <div class="profile-card">
                <h2>Notification Settings</h2>
                
                <div class="profile-section">
                    <h3>Daily Reminder</h3>
                    <p class="notify-desc">Receive a daily email with your schedules and deadlines for the day.</p>
                    
                    <form action="profile" method="POST" class="profile-form">
                        <input type="hidden" name="action" value="updateNotification">
                        
                        <div class="form-group checkbox-group">
                            <label class="checkbox-label">
                                <input type="checkbox" name="notificationEnabled" 
                                       <%= user.getNotificationEnabled() ? "checked" : "" %>>
                                <span>Enable daily reminder email</span>
                            </label>
                        </div>
                        
                        <div class="form-group">
                            <label for="notificationHour">Notification Time</label>
                            <select id="notificationHour" name="notificationHour">
                                <% for (int h = 0; h < 24; h++) { %>
                                    <option value="<%= h %>" <%= user.getNotificationHour() == h ? "selected" : "" %>>
                                        <%= String.format("%02d:00", h) %>
                                    </option>
                                <% } %>
                            </select>
                        </div>
                        
                        <button type="submit" class="btn-primary">Save Settings</button>
                    </form>
                </div>
                
                <!-- Logout Section -->
                <div class="logout-section">
                    <a href="logout" class="btn-logout">Logout</a>
                </div>
            </div>
        </div>
    </main>
</body>
</html>
