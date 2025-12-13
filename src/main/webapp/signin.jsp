<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0"> <title>Đăng Nhập | Todo App</title>
        <link rel="stylesheet" href="css/style.css">
    </head>
    <body>
        
        <main class="auth-container">
            <header class="auth-header">
                <h2>Đăng Nhập</h2>
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
                    <label for="email">Địa chỉ Email</label>
                    <input type="email" id="email" name="email" class="form-control" 
                           placeholder="Ví dụ: admin@gmail.com" required autofocus>
                </div>

                <div class="form-group">
                    <label for="password">Mật khẩu</label>
                    <input type="password" id="password" name="password" class="form-control" 
                           placeholder="Nhập mật khẩu của bạn" required>
                </div>

                <button type="submit" class="btn-submit">Đăng Nhập Ngay</button>
            </form>

            <footer class="auth-footer">
                Chưa có tài khoản? <a href="register">Đăng ký tại đây</a>
            </footer>
        </main>

    </body>
</html>