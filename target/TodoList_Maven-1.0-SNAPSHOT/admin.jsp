<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin - User Management</title>
    <link rel="stylesheet" href="css/admin.css">
</head>
<body>
    <div class="admin-container">
        <div class="admin-header">
            <h1>User Management</h1>
            <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Logout</a>
        </div>
        
        <div class="admin-content">
            <table class="user-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Email</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="user" items="${users}">
                        <tr>
                            <td>${user.id}</td>
                            <td>${user.email}</td>
                            <td>
                                <span class="status ${user.isBlocked ? 'blocked' : 'active'}">
                                    ${user.isBlocked ? 'Blocked' : 'Active'}
                                </span>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${user.isBlocked}">
                                        <form method="post" style="display:inline;">
                                            <input type="hidden" name="userId" value="${user.id}">
                                            <input type="hidden" name="action" value="unblock">
                                            <button type="submit" class="btn-action btn-unblock">Unblock</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <form method="post" style="display:inline;">
                                            <input type="hidden" name="userId" value="${user.id}">
                                            <input type="hidden" name="action" value="block">
                                            <button type="submit" class="btn-action btn-block">Block</button>
                                        </form>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty users}">
                        <tr>
                            <td colspan="4" style="text-align: center; color: #666;">No users found</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>
