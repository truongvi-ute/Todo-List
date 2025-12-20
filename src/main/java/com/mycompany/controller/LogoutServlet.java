package com.mycompany.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet xử lý đăng xuất người dùng.
 * URL: /logout
 */
@WebServlet(urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    /**
     * Xử lý GET request - Đăng xuất.
     * Hủy session hiện tại và chuyển về trang đăng nhập.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Lấy session hiện tại
        HttpSession session = request.getSession();
        
        // Hủy session (xóa thông tin user đã đăng nhập)
        session.invalidate();
        
        // Chuyển về trang đăng nhập
        response.sendRedirect(request.getContextPath() + "/login");
    }
}
