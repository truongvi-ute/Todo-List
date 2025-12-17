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
                <h2>Sign In</h2>
            </header>

            <% 
                String message = (String) request.getAttribute("message");
                if (message != null) {
            %>
                <div class="alert alert-error">
                    <%= message %>
                </div>
            <% } %>

            <form action="login" method="POST">
                <div class="form-group">
                    <label for="email">Email Address</label>
                    <input type="email" id="email" name="email" class="form-control" 
                           placeholder="e.g. admin@gmail.com" required autofocus>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" class="form-control" 
                           placeholder="Enter your password" required>
                </div>

                <button type="submit" class="btn-submit">Sign In</button>
            </form>

            <div class="forgot-password">
                <a href="forgot-password">Forgot password?</a>
            </div>

            <footer class="auth-footer">
                Don't have an account? <a href="register">Sign up here</a>
            </footer>
        </main>

    </body>
</html>