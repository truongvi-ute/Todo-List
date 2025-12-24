<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/header.css">

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
