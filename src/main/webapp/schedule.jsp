<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TodoList - Schedule</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/img/logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/schedule.css">
</head>
<body>
    <jsp:include page="header.jsp" />
    
    <main class="main-container">
        <!-- DASHBOARD CONTROLS: 2 FORMS -->
        <div class="dashboard-controls">
            <!-- LEFT: MANAGE DAY EVENT -->
            <div class="exception-panel">
                <h3>Manage Day Event</h3>
                <form id="dayEventForm" method="post" action="${pageContext.request.contextPath}/schedule">
                    <input type="hidden" name="action" id="dayEventAction" value="cancelDay">
                    <input type="hidden" name="dayEventId" id="dayEventId" value="">
                    
                    <div class="form-group">
                        <label>Event Name</label>
                        <input type="text" id="selectedEventName" class="form-input" readonly placeholder="Click an event on calendar">
                    </div>
                    
                    <div class="form-group">
                        <label>Date</label>
                        <input type="text" id="selectedEventDate" class="form-input" readonly placeholder="-">
                    </div>
                    
                    <div class="form-group">
                        <label>Action</label>
                        <div class="radio-group-vertical">
                            <label class="radio-label">
                                <input type="radio" name="dayAction" value="cancel" checked onchange="updateDayEventAction()">
                                Cancel this day
                            </label>
                            <label class="radio-label">
                                <input type="radio" name="dayAction" value="restore" onchange="updateDayEventAction()">
                                Restore this day
                            </label>
                            <label class="radio-label">
                                <input type="radio" name="dayAction" value="override" onchange="updateDayEventAction()">
                                Change time
                            </label>
                        </div>
                    </div>
                    
                    <!-- Show when "Change time" selected -->
                    <div class="form-group override-time-group" style="display: none;">
                        <label>New Time</label>
                        <div class="time-inputs">
                            <input type="time" name="newStartTime" class="form-input">
                            <span>to</span>
                            <input type="time" name="newEndTime" class="form-input">
                        </div>
                    </div>
                    
                    <div class="button-group">
                        <button type="submit" class="btn-submit" id="btnDayEventSubmit" disabled>Apply</button>
                    </div>
                </form>
            </div>
            
            <!-- RIGHT: ADD/EDIT SCHEDULE -->
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
                    
                    <!-- Row 2: Start time, End time -->
                    <div class="form-row">
                        <div class="form-group flex-1">
                            <label>Start Time (06:00 - 23:59)</label>
                            <input type="time" name="startTime" id="startTime" class="form-input" min="06:00" max="23:59" required>
                        </div>
                        <div class="form-group flex-1">
                            <label>End Time (06:00 - 23:59)</label>
                            <input type="time" name="endTime" id="endTime" class="form-input" min="06:00" max="23:59" required>
                        </div>
                    </div>
                    
                    <!-- Row 3: Start date, End date -->
                    <div class="form-row">
                        <div class="form-group flex-1">
                            <label>Start Date</label>
                            <input type="date" name="startDate" id="startDate" class="form-input" required>
                        </div>
                        <div class="form-group flex-1">
                            <label>End Date</label>
                            <input type="date" name="endDate" id="endDate" class="form-input" required>
                        </div>
                    </div>
                    
                    <!-- Days of week for recurrence -->
                    <div class="form-group">
                        <label>Repeat on Days (leave empty for single event)</label>
                        <div class="checkbox-group">
                            <label class="checkbox-label"><input type="checkbox" name="days" value="MON"> Mon</label>
                            <label class="checkbox-label"><input type="checkbox" name="days" value="TUE"> Tue</label>
                            <label class="checkbox-label"><input type="checkbox" name="days" value="WED"> Wed</label>
                            <label class="checkbox-label"><input type="checkbox" name="days" value="THU"> Thu</label>
                            <label class="checkbox-label"><input type="checkbox" name="days" value="FRI"> Fri</label>
                            <label class="checkbox-label"><input type="checkbox" name="days" value="SAT"> Sat</label>
                            <label class="checkbox-label"><input type="checkbox" name="days" value="SUN"> Sun</label>
                        </div>
                        <input type="hidden" name="recurrenceDays" id="recurrenceDays" value="">
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
                        <button type="button" class="btn-cancel" id="btnCancel" style="display: none;" onclick="resetScheduleForm()">Cancel</button>
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
                            <div class="hour-slot" data-hour="${hour}"></div>
                        </c:forEach>
                        
                        <!-- DayEvents overlay -->
                        <c:forEach var="event" items="${day.events}">
                            <c:set var="startHour" value="${event.effectiveStartTime.hour}" />
                            <c:set var="startMin" value="${event.effectiveStartTime.minute}" />
                            <c:set var="endHour" value="${event.effectiveEndTime.hour}" />
                            <c:set var="endMin" value="${event.effectiveEndTime.minute}" />
                            <c:set var="topPos" value="${(startHour - 6) * 50 + startMin * 50 / 60}" />
                            <c:set var="height" value="${(endHour - startHour) * 50 + (endMin - startMin) * 50 / 60}" />
                            <c:if test="${height < 25}"><c:set var="height" value="25" /></c:if>
                            <div class="event-card ${event.status == 'CANCELLED' ? 'cancelled' : ''}" 
                                 data-day-event-id="${event.id}"
                                 data-schedule-event-id="${event.scheduleEvent.id}"
                                 data-title="${event.scheduleEvent.title}"
                                 data-description="${event.scheduleEvent.description}"
                                 data-specific-date="${event.specificDate}"
                                 data-start-hour="${startHour}"
                                 data-start-minute="${startMin}"
                                 data-end-hour="${endHour}"
                                 data-end-minute="${endMin}"
                                 data-status="${event.status}"
                                 data-start-date="${event.scheduleEvent.startDate}"
                                 data-end-date="${event.scheduleEvent.endDate}"
                                 data-recurrence-days="${event.scheduleEvent.recurrenceDays}"
                                 style="top: ${topPos}px; height: ${height}px;">
                                <span class="event-title">${event.scheduleEvent.title}</span>
                                <c:if test="${event.status == 'CANCELLED'}">
                                    <span class="event-status">(Cancelled)</span>
                                </c:if>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:forEach>
        </div>
    </main>
    
    <script>
        // Update recurrenceDays hidden field before submit
        document.getElementById('scheduleForm').addEventListener('submit', function(e) {
            const checkboxes = document.querySelectorAll('input[name="days"]:checked');
            const days = Array.from(checkboxes).map(cb => cb.value);
            document.getElementById('recurrenceDays').value = days.join(',');
            
            // Validate dates
            const startDate = new Date(document.getElementById('startDate').value);
            const endDate = new Date(document.getElementById('endDate').value);
            
            if (endDate < startDate) {
                e.preventDefault();
                alert('End date cannot be before start date!');
                return false;
            }
            
            // Check date range <= 1 year
            const oneYearLater = new Date(startDate);
            oneYearLater.setFullYear(oneYearLater.getFullYear() + 1);
            if (endDate > oneYearLater) {
                e.preventDefault();
                alert('Date range cannot exceed 1 year!');
                return false;
            }
            
            // Validate times
            const startTime = document.getElementById('startTime').value;
            const endTime = document.getElementById('endTime').value;
            
            if (endTime <= startTime) {
                e.preventDefault();
                alert('End time must be after start time!');
                return false;
            }
        });
        
        // Toggle hiển thị override time options
        function updateDayEventAction() {
            const selectedAction = document.querySelector('input[name="dayAction"]:checked').value;
            const overrideGroup = document.querySelector('.override-time-group');
            const actionInput = document.getElementById('dayEventAction');
            
            overrideGroup.style.display = selectedAction === 'override' ? 'block' : 'none';
            
            if (selectedAction === 'cancel') {
                actionInput.value = 'cancelDay';
            } else if (selectedAction === 'restore') {
                actionInput.value = 'restoreDay';
            } else {
                actionInput.value = 'overrideTime';
            }
        }
        
        var isEditingSchedule = false;
        var selectedDayEventId = null;
        
        // Format time to HH:MM
        function formatTime(hour, minute) {
            return String(hour).padStart(2, '0') + ':' + String(minute).padStart(2, '0');
        }
        
        // Click vào event card
        document.querySelectorAll('.event-card').forEach(card => {
            card.addEventListener('click', function(e) {
                e.stopPropagation();
                
                // Populate Day Event form (left panel)
                selectedDayEventId = this.dataset.dayEventId;
                document.getElementById('dayEventId').value = selectedDayEventId;
                document.getElementById('selectedEventName').value = this.dataset.title;
                document.getElementById('selectedEventDate').value = this.dataset.specificDate;
                document.getElementById('btnDayEventSubmit').disabled = false;
                
                // Set appropriate radio based on status
                if (this.dataset.status === 'CANCELLED') {
                    document.querySelector('input[name="dayAction"][value="restore"]').checked = true;
                } else {
                    document.querySelector('input[name="dayAction"][value="cancel"]').checked = true;
                }
                updateDayEventAction();
                
                // Populate Schedule form (right panel) for editing parent
                isEditingSchedule = true;
                document.getElementById('editEventId').value = this.dataset.scheduleEventId;
                document.getElementById('eventTitle').value = this.dataset.title;
                document.getElementById('eventDescription').value = this.dataset.description || '';
                document.getElementById('startDate').value = this.dataset.startDate;
                document.getElementById('endDate').value = this.dataset.endDate;
                document.getElementById('startTime').value = formatTime(
                    parseInt(this.dataset.startHour), parseInt(this.dataset.startMinute));
                document.getElementById('endTime').value = formatTime(
                    parseInt(this.dataset.endHour), parseInt(this.dataset.endMinute));
                
                // Set recurrence days checkboxes
                const recurrenceDays = this.dataset.recurrenceDays || '';
                document.querySelectorAll('input[name="days"]').forEach(cb => {
                    cb.checked = recurrenceDays.includes(cb.value);
                });
                
                // Update form state
                document.querySelector('#scheduleForm input[name="action"]').value = 'edit';
                document.getElementById('btnDelete').style.display = 'inline-block';
                document.getElementById('btnCancel').style.display = 'inline-block';
                document.querySelector('.add-schedule-panel h3').textContent = 'Edit Schedule';
            });
        });
        
        // Reset schedule form to add mode
        function resetScheduleForm() {
            isEditingSchedule = false;
            document.getElementById('editEventId').value = '';
            document.getElementById('btnDelete').style.display = 'none';
            document.getElementById('btnCancel').style.display = 'none';
            document.querySelector('#scheduleForm input[name="action"]').value = 'add';
            document.querySelector('.add-schedule-panel h3').textContent = 'Add Schedule';
            document.getElementById('scheduleForm').reset();
        }
        
        // Click outside to cancel edit
        document.addEventListener('click', function(e) {
            if (!isEditingSchedule) return;
            
            const schedulePanel = document.querySelector('.add-schedule-panel');
            let clickedOnEvent = false;
            
            document.querySelectorAll('.event-card').forEach(card => {
                if (card.contains(e.target)) clickedOnEvent = true;
            });
            
            if (!schedulePanel.contains(e.target) && !clickedOnEvent) {
                resetScheduleForm();
            }
        });
        
        // Xử lý nút Xóa
        document.getElementById('btnDelete').addEventListener('click', function() {
            const eventId = document.getElementById('editEventId').value;
            if (eventId && confirm('Delete this schedule and all its events?')) {
                document.querySelector('#scheduleForm input[name="action"]').value = 'delete';
                document.getElementById('scheduleForm').submit();
            }
        });
    </script>
</body>
</html>
