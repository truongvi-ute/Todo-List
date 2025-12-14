<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Sign Up | Todo App</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>

        <main class="auth-container">
            <header class="auth-header">
                <h2>Create Account</h2>
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

            <form action="register" method="POST">
                <div class="form-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" class="form-control" 
                           placeholder="Enter your email" required>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" class="form-control" 
                           placeholder="Create a password" required minlength="6">
                </div>

                <div class="form-group">
                    <label for="confirmPassword">Confirm Password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" 
                           placeholder="Re-enter password" required>
                </div>

                <button type="submit" class="btn-submit">Sign Up</button>
            </form>

            <footer class="auth-footer">
                Already have an account? <a href="login">Sign in</a>
            </footer>
        </main>

    </body>
</html>