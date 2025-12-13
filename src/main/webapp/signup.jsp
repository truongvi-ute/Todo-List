<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Đăng Ký | Todo App</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>

        <main class="auth-container">
            <header class="auth-header">
                <h2>Tạo Tài Khoản</h2>
            </header>

            <% 
                String msg = (String) request.getAttribute("message");
                if (msg != null) {
                    String alertClass = msg.toLowerCase().contains("thành công") ? "alert-success" : "alert-error";
            %>
                <div class="alert <%= alertClass %>">
                    <%= msg %>
                </div>
            <% } %>

            <form action="register" method="POST">
                <div class="form-group">
                    <label for="email">Email đăng nhập</label>
                    <input type="email" id="email" name="email" class="form-control" 
                           placeholder="Nhập email của bạn" required>
                </div>

                <div class="form-group">
                    <label for="password">Mật khẩu</label>
                    <input type="password" id="password" name="password" class="form-control" 
                           placeholder="Tự tạo mật khẩu" required minlength="6">
                </div>

                <div class="form-group">
                    <label for="confirmPassword">Xác nhận mật khẩu</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" 
                           placeholder="Nhập lại mật khẩu bên trên" required>
                </div>

                <button type="submit" class="btn-submit">Đăng Ký</button>
            </form>

            <footer class="auth-footer">
                Đã có tài khoản rồi? <a href="login">Đăng nhập ngay</a>
            </footer>
        </main>

    </body>
</html>