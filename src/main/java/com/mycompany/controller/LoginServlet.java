package com.mycompany.controller;

import com.mycompany.data.AdminDB;
import com.mycompany.data.UserDB;
import com.mycompany.model.Admin;
import com.mycompany.service.PasswordUtil;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import com.mycompany.model.User;

@WebServlet(urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    /**
     * Xử lý POST request - Đăng nhập.
     * Kiểm tra cả bảng Admin và User.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String pass = request.getParameter("password");
        
        String url = "/signin.jsp";
        
        // Kiểm tra Admin trước
        Admin admin = AdminDB.selectAdmin(email);
        if (admin != null && PasswordUtil.verify(pass, admin.getPassword())) {
            HttpSession session = request.getSession();
            session.setAttribute("loginedAdmin", admin);
            session.setMaxInactiveInterval(30 * 60);
            response.sendRedirect(request.getContextPath() + "/admin");
            return;
        }
        
        // Kiểm tra User
        User user = UserDB.selectUser(email);
        if (user != null && PasswordUtil.verify(pass, user.getPassword())) {
            // Kiểm tra user có bị chặn không
            if (user.getIsBlocked()) {
                request.setAttribute("message", "Your account has been blocked. Please contact admin.");
                getServletContext().getRequestDispatcher(url).forward(request, response);
                return;
            }
            
            // Đăng nhập thành công
            HttpSession session = request.getSession();
            session.setAttribute("loginedUser", user);
            session.setMaxInactiveInterval(30 * 60);
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        request.setAttribute("message", "Invalid email or password!");
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
    
    /**
     * Xử lý GET request - Hiển thị trang đăng nhập.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("message") != null) {
            request.setAttribute("message", session.getAttribute("message"));
            session.removeAttribute("message");
        }
        getServletContext().getRequestDispatcher("/signin.jsp").forward(request, response);
    }
}
