package com.mycompany.controller;

import com.mycompany.data.UserDB;
import com.mycompany.model.Admin;
import com.mycompany.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * Servlet quản lý Admin.
 * Chức năng: Xem danh sách users, chặn/bỏ chặn user.
 */
@WebServlet(urlPatterns = {"/admin"})
public class AdminServlet extends HttpServlet {

    /**
     * GET - Hiển thị trang quản lý users.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Admin admin = (Admin) session.getAttribute("loginedAdmin");
        
        // Kiểm tra quyền Admin
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Lấy danh sách users
        List<User> users = UserDB.getAllUsers();
        request.setAttribute("users", users);
        
        getServletContext().getRequestDispatcher("/admin.jsp").forward(request, response);
    }

    /**
     * POST - Xử lý chặn/bỏ chặn user.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Admin admin = (Admin) session.getAttribute("loginedAdmin");
        
        // Kiểm tra quyền Admin
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String action = request.getParameter("action");
        String userIdStr = request.getParameter("userId");
        
        if (userIdStr != null && !userIdStr.isEmpty()) {
            Long userId = Long.parseLong(userIdStr);
            
            if ("block".equals(action)) {
                UserDB.updateBlockStatus(userId, true);
            } else if ("unblock".equals(action)) {
                UserDB.updateBlockStatus(userId, false);
            }
        }
        
        response.sendRedirect(request.getContextPath() + "/admin");
    }
}
