<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TodoList</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/img/logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/schedule.css">
</head>
<body>
    <jsp:include page="header.jsp" />
    
    <main class="main-container">
        <!-- DASHBOARD CONTROLS: 2 FORMS -->
        <div class="dashboard-controls">
            <!-- LEFT: ADD EXCEPTION -->
            <div class="exception-panel">
                <h3>Add Exception</h3>
                <form id="exceptionForm" method="post" action="${pageContext.request.contextPath}/schedule">
                    <input type="hidden" name="action" value="addException">
                    
                    <div class="form-group">
                        <label>Select Schedule</label>
                        <select name="eventId" class="form-input" required>
                            <option value="">-- Select schedule --</option>
                            <c:forEach var="event" items="${recurringEvents}">
                                <option value="${event.id}">${event.title}</option>
                            </c:forEach>
                        </select>
                    </div>
                    
                    <div class="form-group">
                        <label>Exception Date</label>
                        <input type="date" name="exceptionDate" class="form-input" required>
                    </div>
                    
                    <div class="form-group">
                        <label>Exception Type</label>
                        <div class="radio-group-vertical">
                            <label class="radio-label">
                                <input type="radio" name="exceptionType" value="skip" checked>
                                Skip this day
                            </label>
                            <label class="radio-label">
                                <input type="radio" name="exceptionType" value="add">
                                Add this day
                            </label>
                            <label class="radio-label">
                                <input type="radio" name="exceptionType" value="modify">
                                Change time
                            </label>
                        </div>
                    </div>
                    
                    <!-- Show when "Change time" selected -->
                    <div class="form-group modify-time-group" style="display: none;">
                        <label>New Time</label>
                        <div class="time-inputs">
                            <input type="time" name="newStartTime" class="form-input">
                            <span>to</span>
                            <input type="time" name="newEndTime" class="form-input">
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label>Description</label>
                        <textarea name="exceptionDescription" class="form-textarea" placeholder="Reason for exception..."></textarea>
                    </div>
                    
                    <div class="button-group">
                        <button type="submit" class="btn-submit">Add</button>
                    </div>
                </form>
            </div>
            
            <!-- RIGHT: ADD SCHEDULE -->
            <div class="add-schedule-panel">
                <h3>Add Schedule</h3>
                <form id="scheduleForm" method="post" action="${pageContext.request.contextPath}/schedule">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="eventId" id="editEventId" value="">
                    
                    <!-- Row 1: Title -->
                    <div class="form-group">
                        <label>Title</label>
                        <input type="text" name="title" id="eventTitle" class="form-input" placeholder="Event name..." required>
                    </div>
                    
                    <!-- Row 2: Start time, End time, Repeat -->
                    <div class="form-row">
                        <div class="form-group flex-1">
                            <label>Start Time (06:00 - 23:00)</label>
                            <input type="time" name="startTime" id="startTime" class="form-input" min="06:00" max="23:00" required>
                        </div>
                        <div class="form-group flex-1">
                            <label>End Time (06:00 - 23:59)</label>
                            <input type="time" name="endTime" id="endTime" class="form-input" min="06:00" max="23:59" required>
                        </div>
                        <div class="form-group flex-1">
                            <label>Repeat</label>
                            <select name="frequency" id="frequency" class="form-input">
                                <option value="NONE">No repeat</option>
                                <option value="DAILY">Daily</option>
                                <option value="WEEKLY">Weekly</option>
                            </select>
                        </div>
                    </div>
                    
                    <!-- Row 3: Start date, Until date (show when repeat) -->
                    <div class="form-row">
                        <div class="form-group flex-1">
                            <label>Start Date</label>
                            <input type="date" name="eventDate" id="eventDate" class="form-input" required>
                        </div>
                        <div class="form-group flex-1 until-date-group" style="display: none;">
                            <label>Until Date</label>
                            <input type="date" name="untilDate" id="untilDate" class="form-input">
                        </div>
                    </div>
                    
                    <!-- Days of week (show when WEEKLY) -->
                    <div class="form-group weekly-days" style="display: none;">
                        <label>Days of Week</label>
                        <div class="checkbox-group">
                            <label class="checkbox-label"><input type="checkbox" name="byDays" value="MONDAY"> Mon</label>
                            <label class="checkbox-label"><input type="checkbox" name="byDays" value="TUESDAY"> Tue</label>
                            <label class="checkbox-label"><input type="checkbox" name="byDays" value="WEDNESDAY"> Wed</label>
                            <label class="checkbox-label"><input type="checkbox" name="byDays" value="THURSDAY"> Thu</label>
                            <label class="checkbox-label"><input type="checkbox" name="byDays" value="FRIDAY"> Fri</label>
                            <label class="checkbox-label"><input type="checkbox" name="byDays" value="SATURDAY"> Sat</label>
                            <label class="checkbox-label"><input type="checkbox" name="byDays" value="SUNDAY"> Sun</label>
                        </div>
                    </div>
                    
                    <!-- Row 4: Description -->
                    <div class="form-group">
                        <label>Description</label>
                        <textarea name="description" id="eventDescription" class="form-textarea" placeholder="Notes..."></textarea>
                    </div>
                    
                    <!-- Error Message -->
                    <c:if test="${not empty param.error}">
                        <div class="error-message" style="color: red; margin-bottom: 10px;">${param.error}</div>
                    </c:if>
                    
                    <div class="button-group">
                        <button type="button" class="btn-delete" id="btnDelete" style="display: none;">Delete</button>
                        <button type="submit" class="btn-submit">Save</button>
                    </div>
                </form>
            </div>
        </div>
        
        <!-- WEEK NAVIGATION -->
        <div class="week-navigation">
            <a href="${pageContext.request.contextPath}/schedule?action=prevWeek" class="nav-btn">◀ Prev Week</a>
            <span class="current-week">
                ${currentMonth} / ${currentYear}
            </span>
            <a href="${pageContext.request.contextPath}/schedule?action=nextWeek" class="nav-btn">Next Week ▶</a>
        </div>
        
        <!-- WEEKLY SCHEDULE GRID -->
        <div class="schedule-grid">
            <!-- Cột thời gian -->
            <div class="time-column">
                <div class="time-header"></div>
                <c:forEach var="hour" begin="6" end="23">
                    <div class="time-slot">
                        <span class="time-label">${hour}:00</span>
                    </div>
                </c:forEach>
            </div>
            
            <!-- 7 cột ngày -->
            <c:forEach var="day" items="${weekDays}" varStatus="dayIndex">
                <div class="day-column">
                    <div class="day-header ${day.date eq today ? 'today' : ''}">
                        <span class="day-name">${day.dayName}</span>
                        <span class="date">${day.dateString}</span>
                    </div>
                    <div class="day-slots" data-date="${day.date}">
                        <!-- 18 ô từ 6h đến 23h -->
                        <c:forEach var="hour" begin="6" end="23">
                            <div class="hour-slot" data-hour="${hour}">
                                <!-- Events sẽ được render ở đây -->
                            </div>
                        </c:forEach>
                        
                        <!-- Events overlay -->
                        <c:forEach var="event" items="${day.events}">
                            <c:set var="startHour" value="${event.startTime.hour}" />
                            <c:set var="startMin" value="${event.startTime.minute}" />
                            <c:set var="endHour" value="${event.endTime.hour}" />
                            <c:set var="endMin" value="${event.endTime.minute}" />
                            <c:set var="topPos" value="${(startHour - 6) * 50 + startMin * 50 / 60}" />
                            <c:set var="height" value="${(endHour - startHour) * 50 + (endMin - startMin) * 50 / 60}" />
                            <c:if test="${height < 25}"><c:set var="height" value="25" /></c:if>
                            <div class="event-card" 
                                 data-event-id="${event.id}"
                                 data-title="${event.title}"
                                 data-description="${event.description}"
                                 data-date="${day.date}"
                                 data-original-date="${event.startTime.toLocalDate()}"
                                 data-start-hour="${startHour}"
                                 data-start-minute="${startMin}"
                                 data-end-hour="${endHour}"
                                 data-end-minute="${endMin}"
                                 data-frequency="${event.recurrenceRule != null ? event.recurrenceRule.frequency : 'NONE'}"
                                 data-until-date="${event.recurrenceRule != null ? event.recurrenceRule.untilDate : ''}"
                                 style="top: ${topPos}px; height: ${height}px;">
                                <span class="event-title">${event.title}</span>
                                <c:if test="${not empty event.description}">
                                    <span class="event-desc">${event.description}</span>
                                </c:if>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:forEach>
        </div>
    </main>
    
    <script>
        // Toggle hiển thị options khi chọn loại ngoại lệ
        document.querySelectorAll('input[name="exceptionType"]').forEach(radio => {
            radio.addEventListener('change', function() {
                const modifyGroup = document.querySelector('.modify-time-group');
                modifyGroup.style.display = this.value === 'modify' ? 'block' : 'none';
            });
        });
        
        // Toggle hiển thị recurrence options
        document.getElementById('frequency').addEventListener('change', function() {
            const untilDateGroup = document.querySelector('.until-date-group');
            const weeklyDays = document.querySelector('.weekly-days');
            
            if (this.value === 'NONE') {
                untilDateGroup.style.display = 'none';
                weeklyDays.style.display = 'none';
            } else {
                untilDateGroup.style.display = 'block';
                weeklyDays.style.display = this.value === 'WEEKLY' ? 'block' : 'none';
            }
        });
        
        var isEditing = false;
        
        // Format time to HH:MM
        function formatTime(hour, minute) {
            return String(hour).padStart(2, '0') + ':' + String(minute).padStart(2, '0');
        }
        
        // Click vào event để edit
        document.querySelectorAll('.event-card').forEach(card => {
            card.addEventListener('click', function() {
                isEditing = true;
                
                // Get data from event card
                const eventId = this.dataset.eventId;
                const title = this.dataset.title;
                const description = this.dataset.description || '';
                const originalDate = this.dataset.originalDate;
                const startHour = parseInt(this.dataset.startHour);
                const startMinute = parseInt(this.dataset.startMinute);
                const endHour = parseInt(this.dataset.endHour);
                const endMinute = parseInt(this.dataset.endMinute);
                const frequency = this.dataset.frequency || 'NONE';
                const untilDate = this.dataset.untilDate || '';
                
                // Populate form
                document.getElementById('editEventId').value = eventId;
                document.getElementById('eventTitle').value = title;
                document.getElementById('eventDescription').value = description;
                document.getElementById('eventDate').value = originalDate;
                document.getElementById('startTime').value = formatTime(startHour, startMinute);
                document.getElementById('endTime').value = formatTime(endHour, endMinute);
                
                // Set frequency and show/hide related fields
                document.getElementById('frequency').value = frequency;
                const untilDateGroup = document.querySelector('.until-date-group');
                const weeklyDays = document.querySelector('.weekly-days');
                
                if (frequency !== 'NONE') {
                    untilDateGroup.style.display = 'block';
                    if (untilDate) {
                        document.getElementById('untilDate').value = untilDate;
                    }
                    weeklyDays.style.display = frequency === 'WEEKLY' ? 'block' : 'none';
                } else {
                    untilDateGroup.style.display = 'none';
                    weeklyDays.style.display = 'none';
                }
                
                // Update form state
                document.querySelector('#scheduleForm input[name="action"]').value = 'edit';
                document.getElementById('btnDelete').style.display = 'inline-block';
                document.querySelector('.add-schedule-panel h3').textContent = 'Edit Schedule';
                
                // Scroll to form
                document.querySelector('.add-schedule-panel').scrollIntoView({ behavior: 'smooth' });
            });
        });
        
        // Reset form to add mode
        function resetForm() {
            isEditing = false;
            document.getElementById('editEventId').value = '';
            document.getElementById('btnDelete').style.display = 'none';
            document.querySelector('#scheduleForm input[name="action"]').value = 'add';
            document.querySelector('.add-schedule-panel h3').textContent = 'Add Schedule';
            document.getElementById('scheduleForm').reset();
        }
        
        // Click outside form để cancel edit
        document.addEventListener('click', function(e) {
            if (!isEditing) return;
            
            const form = document.getElementById('scheduleForm');
            const panel = document.querySelector('.add-schedule-panel');
            let clickedOnEvent = false;
            
            document.querySelectorAll('.event-card').forEach(card => {
                if (card.contains(e.target)) clickedOnEvent = true;
            });
            
            if (!panel.contains(e.target) && !clickedOnEvent) {
                resetForm();
            }
        });
        
        // Xử lý nút Xóa
        document.getElementById('btnDelete').addEventListener('click', function() {
            const eventId = document.getElementById('editEventId').value;
            if (eventId) {
                document.querySelector('#scheduleForm input[name="action"]').value = 'delete';
                document.getElementById('scheduleForm').submit();
            }
        });
    </script>
</body>
</html>
