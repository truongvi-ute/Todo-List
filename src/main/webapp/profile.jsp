<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>

<c:if test="${empty sessionScope.loginedUser}">
    <c:redirect url="login"/>
</c:if>
<c:set var="user" value="${sessionScope.loginedUser}"/>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TodoList</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/img/logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/profile.css">
</head>
<body>
    <jsp:include page="header.jsp" />
    
    <main class="profile-container">
        <c:if test="${not empty message}">
            <c:set var="alertClass" value="${fn:containsIgnoreCase(message, 'success') ? 'alert-success' : 'alert-error'}"/>
            <div class="alert ${alertClass}">${message}</div>
        </c:if>
        
        <div class="profile-grid">
            <!-- Left: Edit Profile -->
            <div class="profile-card">
                <h2>Edit Profile</h2>
                
                <div class="profile-section">
                    <h3>Account Information</h3>
                    <p class="email-display"><strong>Email:</strong> ${user.email}</p>
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
                                       ${user.notificationEnabled ? 'checked' : ''}>
                                <span>Enable daily reminder email</span>
                            </label>
                        </div>
                        
                        <div class="form-group">
                            <label for="notificationHour">Notification Time</label>
                            <select id="notificationHour" name="notificationHour">
                                <c:forEach var="h" begin="0" end="23">
                                    <option value="${h}" ${user.notificationHour == h ? 'selected' : ''}>
                                        ${h < 10 ? '0' : ''}${h}:00
                                    </option>
                                </c:forEach>
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
