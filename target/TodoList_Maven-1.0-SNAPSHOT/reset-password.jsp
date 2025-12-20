<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TodoList</title>
    <link rel="icon" type="image/png" href="img/logo.png">
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <main class="auth-container">
        <header class="auth-header">
            <h2>Reset Password</h2>
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

        <form action="reset-password" method="POST">
            <div class="form-group">
                <label for="password">New Password</label>
                <input type="password" id="password" name="password" class="form-control" 
                       placeholder="Enter new password" required minlength="6">
            </div>

            <div class="form-group">
                <label for="confirmPassword">Confirm Password</label>
                <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" 
                       placeholder="Re-enter password" required>
            </div>

            <button type="submit" class="btn-submit">Reset Password</button>
        </form>

        <footer class="auth-footer">
            <a href="login">Back to Sign In</a>
        </footer>
    </main>
</body>
</html>
