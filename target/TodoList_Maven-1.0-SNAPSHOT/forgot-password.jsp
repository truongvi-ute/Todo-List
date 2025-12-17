<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TodoList</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <main class="auth-container">
        <header class="auth-header">
            <h2>Forgot Password</h2>
        </header>

        <% 
            String msg = (String) request.getAttribute("message");
            if (msg != null) {
                String alertClass = msg.toLowerCase().contains("success") ? "alert-success" : "alert-error";
        %>
            <div class="alert <%= alertClass %>">
                <%= msg %>
            </div>
        <% } %>

        <p class="otp-info">Enter your email to receive a verification code</p>

        <form action="forgot-password" method="POST">
            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" class="form-control" 
                       placeholder="Enter your email" required autofocus>
            </div>

            <button type="submit" class="btn-submit">Send OTP</button>
        </form>

        <footer class="auth-footer">
            <a href="login">Back to Sign In</a>
        </footer>
    </main>
</body>
</html>
