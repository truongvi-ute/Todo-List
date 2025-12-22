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
            <h2>Verify OTP</h2>
        </header>

        <c:if test="${not empty message}">
            <c:set var="alertClass" value="${fn:containsIgnoreCase(message, 'success') or fn:containsIgnoreCase(message, 'sent') ? 'alert-success' : 'alert-error'}"/>
            <div class="alert ${alertClass}">${message}</div>
        </c:if>

        <p class="otp-info">Enter the OTP code sent to your email</p>

        <form action="verify-otp" method="POST">
            <div class="form-group">
                <label for="otp">OTP Code</label>
                <input type="text" id="otp" name="otp" class="form-control otp-input" 
                       placeholder="000000" required maxlength="6" pattern="[0-9]{6}"
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
