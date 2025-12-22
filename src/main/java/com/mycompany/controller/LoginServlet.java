package com.mycompany.controller;

import com.mycompany.data.UserDB;
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
     * Kiểm tra email và password, tạo session nếu thành công.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String pass = request.getParameter("password");
        
        // Tìm user trong database
        User user = UserDB.selectUser(email);
        
        String url = "/signin.jsp";
        
        // Xác thực password bằng BCrypt
        if (user != null && PasswordUtil.verify(pass, user.getPassword())) {
            // Đăng nhập thành công -> Tạo Session
            HttpSession session = request.getSession();
            session.setAttribute("loginedUser", user);
            session.setMaxInactiveInterval(30 * 60); // 30 phút
            url = "/dashboard"; 
            response.sendRedirect(request.getContextPath() + url);
            return;
        } else {
            request.setAttribute("message", "Invalid email or password!");
        }
        
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
    
    /**
     * Xử lý GET request - Hiển thị trang đăng nhập.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lấy message từ session (nếu có) và xóa sau khi dùng
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("message") != null) {
            request.setAttribute("message", session.getAttribute("message"));
            session.removeAttribute("message");
        }
        getServletContext().getRequestDispatcher("/signin.jsp").forward(request, response);
    }
}
