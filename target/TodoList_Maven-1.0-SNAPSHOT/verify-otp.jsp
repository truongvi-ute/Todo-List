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
            <h2>Verify OTP</h2>
        </header>

        <% 
            String msg = (String) request.getAttribute("message");
            if (msg != null) {
                String alertClass = msg.toLowerCase().contains("success") || msg.toLowerCase().contains("sent") 
                    ? "alert-success" : "alert-error";
        %>
            <div class="alert <%= alertClass %>">
                <%= msg %>
            </div>
        <% } %>

        <p class="otp-info">Enter the OTP code sent to your email</p>

        <form action="verify-otp" method="POST">
            <div class="form-group">
                <label for="otp">OTP Code</label>
                <input type="text" id="otp" name="otp" class="form-control otp-input" 
                       placeholder="Enter 6-digit code" required maxlength="6" pattern="[0-9]{6}"
                       autocomplete="one-time-code">
            </div>

            <button type="submit" class="btn-submit">Verify</button>
        </form>

        <footer class="auth-footer">
            <a href="login">Back to Sign In</a>
        </footer>
    </main>
</body>
</html>
