<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib prefix="fn" uri="jakarta.tags.functions"%>
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
            <h2>Forgot Password</h2>
        </header>

        <c:if test="${not empty message}">
            <c:set var="alertClass" value="${fn:containsIgnoreCase(message, 'success') ? 'alert-success' : 'alert-error'}"/>
            <div class="alert ${alertClass}">${message}</div>
        </c:if>

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
