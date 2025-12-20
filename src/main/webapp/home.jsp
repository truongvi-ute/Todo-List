<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TodoList</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/img/logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
</head>
<body>
    <jsp:include page="header.jsp" />

    <main class="main-container">
        <div class="dashboard-content">
            <!-- LEFT: Deadline + Events for selected date -->
            <div class="day-info-panel">
                <h3>${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year}</h3>
                
                <!-- Deadline Tasks -->
                <div class="section-block">
                    <h4>Deadlines</h4>
                    <div class="task-list">
                        <c:choose>
                            <c:when test="${empty deadlineTasks}">
                                <p class="empty-message">No deadlines</p>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="task" items="${deadlineTasks}">
                                    <div class="task-item ${task.status}">
                                        <form method="post" action="${pageContext.request.contextPath}/dashboard" class="toggle-form">
                                            <input type="hidden" name="action" value="toggleTask">
                                            <input type="hidden" name="taskId" value="${task.id}">
                                            <input type="hidden" name="date" value="${selectedDate}">
                                            <label class="checkbox-container">
                                                <input type="checkbox" 
                                                       ${task.status == 'DONE' ? 'checked' : ''} 
                                                       ${task.status == 'LATE' ? 'disabled' : ''}
                                                       onchange="this.form.submit()">
                                                <span class="task-title ${task.status == 'DONE' ? 'done' : ''}">${task.title}</span>
                                                <span class="task-priority ${task.priority}">${task.priority}</span>
                                            </label>
                                        </form>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
                
                <!-- Schedule Events -->
                <div class="section-block">
                    <h4>Schedule</h4>
                    <div class="event-list">
                        <c:choose>
                            <c:when test="${empty scheduleEvents}">
                                <p class="empty-message">No events</p>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="event" items="${scheduleEvents}">
                                    <div class="event-item">
                                        <span class="event-time">
                                            ${event.startTime.hour}:${event.startTime.minute < 10 ? '0' : ''}${event.startTime.minute} - 
                                            ${event.endTime.hour}:${event.endTime.minute < 10 ? '0' : ''}${event.endTime.minute}
                                        </span>
                                        <span class="event-title">${event.title}</span>
                                    </div>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
            
            <!-- RIGHT: Monthly Calendar -->
            <div class="calendar-panel">
                <div class="calendar-header">
                    <a href="${pageContext.request.contextPath}/dashboard?date=${selectedDate}&monthOffset=${monthOffset - 1}" class="nav-btn">◀</a>
                    <span class="month-title">${viewMonthValue} / ${viewYear}</span>
                    <a href="${pageContext.request.contextPath}/dashboard?date=${selectedDate}&monthOffset=${monthOffset + 1}" class="nav-btn">▶</a>
                </div>
                
                <div class="calendar-grid">
                    <!-- Week day headers -->
                    <div class="calendar-day-header">Mon</div>
                    <div class="calendar-day-header">Tue</div>
                    <div class="calendar-day-header">Wed</div>
                    <div class="calendar-day-header">Thu</div>
                    <div class="calendar-day-header">Fri</div>
                    <div class="calendar-day-header">Sat</div>
                    <div class="calendar-day-header">Sun</div>
                    
                    <!-- Các ngày trong tháng -->
                    <c:forEach var="day" items="${calendarDays}">
                        <a href="${pageContext.request.contextPath}/dashboard?date=${day}&monthOffset=${monthOffset}" 
                           class="calendar-day 
                                  ${day.monthValue != viewMonth.monthValue ? 'other-month' : ''} 
                                  ${day eq today ? 'today' : ''} 
                                  ${day eq selectedDate ? 'selected' : ''}">
                            ${day.dayOfMonth}
                        </a>
                    </c:forEach>
                </div>
            </div>
        </div>
    </main>
</body>
</html>
