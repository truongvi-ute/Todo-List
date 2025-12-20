<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core" %> 

<c:if test="${empty weekDays}">
    <c:redirect url="/deadline"/>
</c:if>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TodoList</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/img/logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/home.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/deadline.css">
</head>
<body>

    <jsp:include page="header.jsp" />

    <main class="main-container">
        
        <section class="dashboard-controls">
            
            <div class="filter-nav-panel">
                <div class="week-navigation">
                    <a href="deadline?action=prevWeek" class="nav-btn">&lt;</a>
                    <span>${currentMonth} / ${currentYear}</span>
                    <a href="deadline?action=nextWeek" class="nav-btn">&gt;</a>
                </div>

                <div class="filter-tools">
                    <span style="font-weight: bold; color: #555;">Filter:</span>
                    <select class="form-input" onchange="window.location.href=this.value">
                        <option value="deadline?filter=all" ${currentFilter == 'all' ? 'selected' : ''}>All</option>
                        <option value="deadline?filter=in_progress" ${currentFilter == 'in_progress' ? 'selected' : ''}>In Progress</option>
                        <option value="deadline?filter=done" ${currentFilter == 'done' ? 'selected' : ''}>Done</option>
                        <option value="deadline?filter=late" ${currentFilter == 'late' ? 'selected' : ''}>Late</option>
                    </select>

                    <select class="form-input" onchange="window.location.href=this.value">
                        <option value="deadline?sort=date" ${currentSort == 'date' ? 'selected' : ''}>Sort: Recent</option>
                        <option value="deadline?sort=priority" ${currentSort == 'priority' ? 'selected' : ''}>Sort: Priority</option>
                    </select>
                </div>
                
                <div style="margin-top: 10px; font-size: 0.85rem; color: #888; border-top: 1px solid #eee; padding-top: 10px;">
                    <i>* Click checkbox to mark as done/undone.</i>
                </div>
            </div>

            <div class="add-task-panel">
                <h3 id="formTitle">Add New Deadline</h3>
                <form id="taskForm" action="deadline" method="post">
                    <input type="hidden" id="formAction" name="action" value="add">
                    <input type="hidden" id="editTaskId" name="taskId" value="">
                    
                    <div class="form-group">
                        <input type="text" id="taskTitle" name="title" class="form-input" placeholder="Task name..." required>
                    </div>
                    
                    <div class="form-group">
                        <textarea id="taskDescription" name="description" class="form-textarea" placeholder="Description..."></textarea>
                    </div>
                    
                    <div style="display: flex; gap: 20px; align-items: flex-end;">
                        <div class="form-group" style="flex: 1; margin-bottom: 0;">
                            <label for="dueDate" style="font-size: 0.8rem; color: #666; margin-bottom: 5px; display:block;">Due date:</label>
                            <input type="date" id="dueDate" name="dueDate" class="form-input" required min="${todayStr}">
                        </div>
                        
                        <div class="form-group" style="flex: 2; margin-bottom: 0;">
                            <label style="font-size: 0.8rem; color: #666; margin-bottom: 8px; display:block;">Priority:</label>
                            <div class="radio-group">
                                <label class="radio-label">
                                    <input type="radio" id="prioLow" name="priority" value="LOW" checked> Low
                                </label>
                                <label class="radio-label">
                                    <input type="radio" id="prioMedium" name="priority" value="MEDIUM"> Medium
                                </label>
                                <label class="radio-label">
                                    <input type="radio" id="prioHigh" name="priority" value="HIGH"> High
                                </label>
                            </div>
                        </div>
                        
                        <div id="buttonGroup" class="button-group">
                            <button type="button" id="btnDelete" class="btn-delete" onclick="deleteTask()">Delete</button>
                            <button type="submit" class="btn-submit">Save</button>
                        </div>
                    </div>
                </form>
            </div>

        </section>

        <section class="weekly-grid">
            
            <c:forEach var="day" items="${weekDays}">
                <c:set var="isToday" value="${day.date.equals(today)}" />
                <div class="day-column">
                    <div class="day-header ${isToday ? 'today' : ''}">
                        <div>${day.dayName}</div>
                        <span class="date">${day.dateString}</span>
                    </div>
                    
                    <div class="task-list">
                        <c:forEach var="task" items="${day.tasks}">
                            
                            <div class="task-card priority-${task.priority} status-${task.status}">
                                <div style="display: flex; align-items: flex-start; gap: 8px;">
                                    
                                    <form action="deadline" method="post" style="margin:0;">
                                        <input type="hidden" name="action" value="toggleStatus">
                                        <input type="hidden" name="taskId" value="${task.id}">
                                        
                                        <c:set var="isPast" value="${day.date.isBefore(today)}" />
                                        <input type="checkbox" onchange="this.form.submit()" 
                                               style="cursor: ${isPast ? 'not-allowed' : 'pointer'}; margin-top: 4px; transform: scale(1.2); opacity: ${isPast ? '0.5' : '1'};"
                                               title="${isPast ? 'Cannot change past task' : 'Mark as done/undone'}"
                                               ${task.status == 'DONE' ? 'checked' : ''}
                                               ${isPast ? 'disabled' : ''}>
                                    </form>
                                    
                                    <div style="flex-grow: 1; width: 0;">
                                        <span class="task-title">${task.title}</span>
                                        <div class="task-meta">
                                            <c:if test="${task.status != 'LATE'}">
                                                <button type="button" class="btn-detail" 
                                                        onclick="showDetail('${task.id}', '${task.title}', '${task.description}', '${task.dueDate.toLocalDate()}', '${task.priority}')">
                                                    View
                                                </button>
                                            </c:if>
                                            <c:if test="${task.status == 'LATE'}">
                                                <span></span>
                                            </c:if>
                                            <span class="priority-badge">${task.priority}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </c:forEach>
            

        </section>

    </main>
<script>
    var isEditing = false;
    
    // Show task detail in form for editing
    function showDetail(id, title, description, dueDate, priority) {
        isEditing = true;
        document.getElementById('formTitle').textContent = 'Edit Task';
        document.getElementById('formAction').value = 'edit';
        document.getElementById('editTaskId').value = id;
        document.getElementById('taskTitle').value = title;
        document.getElementById('taskDescription').value = description || '';
        document.getElementById('dueDate').value = dueDate;
        
        // Set radio button
        document.getElementById('prioLow').checked = (priority === 'LOW');
        document.getElementById('prioMedium').checked = (priority === 'MEDIUM');
        document.getElementById('prioHigh').checked = (priority === 'HIGH');
        
        // Show delete button
        document.getElementById('btnDelete').style.display = 'block';
        
        // Scroll to form
        document.querySelector('.add-task-panel').scrollIntoView({ behavior: 'smooth' });
    }
    
    // Cancel edit, reset form to add mode
    function cancelEdit() {
        isEditing = false;
        document.getElementById('formTitle').textContent = 'Add New Deadline';
        document.getElementById('formAction').value = 'add';
        document.getElementById('editTaskId').value = '';
        document.getElementById('taskTitle').value = '';
        document.getElementById('taskDescription').value = '';
        document.getElementById('dueDate').value = '';
        document.getElementById('prioLow').checked = true;
        document.getElementById('btnDelete').style.display = 'none';
    }
    
    // Delete task
    function deleteTask() {
        document.getElementById('formAction').value = 'delete';
        document.getElementById('taskForm').submit();
    }
    
    // Click outside form to cancel edit
    document.addEventListener('click', function(e) {
        if (!isEditing) return;
        
        var panel = document.querySelector('.add-task-panel');
        var isClickInside = panel.contains(e.target);
        var isDetailBtn = e.target.classList.contains('btn-detail');
        
        if (!isClickInside && !isDetailBtn) {
            cancelEdit();
        }
    });
</script>
</body>
</html>