<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trang Chủ | Todo List</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
</head>
<body>

    <jsp:include page="header.jsp" />

    <main class="main-content">
        <div class="welcome-section">
            <h1>Xin chào, ${loginedUser.email}! 👋</h1>
            <p>Chúc bạn một ngày làm việc hiệu quả.</p>
        </div>
        
        <div style="text-align: center; color: #888; margin-top: 50px;">
             <h3>(Khu vực Dashboard - Sẽ phát triển sau)</h3>
        </div>
    </main>

</body>
</html>