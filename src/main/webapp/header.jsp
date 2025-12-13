<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.time.format.DateTimeFormatter"%>

<%
    // Xử lý lấy ngày tháng hiện tại (Java 8 Time API)
    LocalDate today = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'Ngày' dd 'tháng' MM 'năm' yyyy");
    String formattedDate = today.format(formatter);
%>

<style>
    /* Reset cơ bản cho header */
    .main-header {
        background-color: #ffffff;
        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        padding: 0 20px;
        height: 70px;
        
        /* Dùng Flexbox để chia 2 bên trái phải */
        display: flex;
        justify-content: space-between;
        align-items: center;
        position: sticky;
        top: 0;
        z-index: 1000;
    }

    /* Phần bên trái: Ngày tháng */
    .header-left .current-date {
        font-weight: bold;
        color: #555;
        font-size: 0.95rem;
        display: flex;
        align-items: center;
        gap: 8px;
    }

    /* Phần bên phải: Navigation */
    .header-right .nav-links {
        list-style: none;
        display: flex;
        align-items: center;
        gap: 20px; /* Khoảng cách giữa các menu */
        margin: 0;
        padding: 0;
    }

    .header-right .nav-links a {
        text-decoration: none;
        color: #333;
        font-weight: 500;
        font-size: 1rem;
        transition: color 0.3s ease;
    }

    .header-right .nav-links a:hover,
    .header-right .nav-links a.active {
        color: #6c5ce7; /* Màu tím chủ đạo */
    }

    /* Nút Logout nổi bật hơn chút */
    .btn-logout {
        padding: 8px 16px;
        border: 1px solid #ff7675;
        color: #ff7675 !important;
        border-radius: 5px;
        transition: all 0.3s ease;
    }

    .btn-logout:hover {
        background-color: #ff7675;
        color: white !important;
    }
</style>

<header class="main-header">
    <div class="header-left">
        <span class="current-date">
            📅 <%= formattedDate %>
        </span>
    </div>

    <div class="header-right">
        <nav>
            <ul class="nav-links">
                <li><a href="home.jsp" class="${pageContext.request.requestURI.endsWith('home.jsp') ? 'active' : ''}">Dashboard</a></li>
                
                <li><a href="deadline" class="${pageContext.request.requestURI.endsWith('deadline.jsp') ? 'active' : ''}">Deadline</a></li>
                
                <li><a href="schedule.jsp" class="${pageContext.request.requestURI.endsWith('schedule.jsp') ? 'active' : ''}">Schedule</a></li>
                
                <li><a href="logout" class="btn-logout">Đăng xuất</a></li>
            </ul>
        </nav>
    </div>
</header>