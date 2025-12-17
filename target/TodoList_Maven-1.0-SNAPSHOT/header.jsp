<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.time.LocalDate"%>
<%@page import="java.time.DayOfWeek"%>
<%@page import="java.time.format.DateTimeFormatter"%>

<%
    // Xử lý lấy ngày tháng hiện tại (Java 8 Time API)
    LocalDate today = LocalDate.now();
    
    // Get day of week
    String[] dayNames = {"", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    int dayOfWeek = today.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
    String dayName = dayNames[dayOfWeek];
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String formattedDate = dayName + ", " + today.format(formatter);
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


</style>

<header class="main-header">
    <div class="header-left">
        <span class="current-date">
            <%= formattedDate %>
        </span>
    </div>

    <div class="header-right">
        <nav>
            <ul class="nav-links">
                <li><a href="dashboard" class="${pageContext.request.requestURI.endsWith('home.jsp') ? 'active' : ''}">Dashboard</a></li>
                
                <li><a href="deadline" class="${pageContext.request.requestURI.endsWith('deadline.jsp') ? 'active' : ''}">Deadline</a></li>
                
                <li><a href="schedule" class="${pageContext.request.requestURI.endsWith('schedule.jsp') ? 'active' : ''}">Schedule</a></li>
                
                <li><a href="profile" class="${pageContext.request.requestURI.endsWith('profile.jsp') ? 'active' : ''}">Profile</a></li>
            </ul>
        </nav>
    </div>
</header>