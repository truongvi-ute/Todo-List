<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>

<style>
    .main-header {
        background-color: #ffffff;
        box-shadow: 0 2px 5px rgba(0,0,0,0.1);
        padding: 0 20px;
        height: 70px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        position: sticky;
        top: 0;
        z-index: 1000;
    }

    .header-left .current-date {
        font-weight: bold;
        color: #6c5ce7;
        font-size: 0.95rem;
        display: flex;
        align-items: center;
        gap: 8px;
    }

    .header-right .nav-links {
        list-style: none;
        display: flex;
        align-items: center;
        gap: 20px;
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
        color: #6c5ce7;
    }
</style>

<header class="main-header">
    <div class="header-left">
        <span class="current-date">${currentDateFormatted}</span>
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
