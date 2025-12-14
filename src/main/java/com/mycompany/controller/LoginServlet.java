package com.mycompany.controller;

import com.mycompany.data.UserDB;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*; // Import Session
import com.mycompany.model.User;

@WebServlet(urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String email = request.getParameter("email");
        String pass = request.getParameter("password");
        
        // Kiểm tra trong Database
        User user = UserDB.selectUser(email);
        
        String url = "/signin.jsp";
        
        if (user != null && user.getPassword().equals(pass)) {
            // Đăng nhập thành công -> Tạo Session
            HttpSession session = request.getSession();
            session.setAttribute("loginedUser", user);
            session.setMaxInactiveInterval(30 * 60);
            url = "/dashboard"; 
            response.sendRedirect(request.getContextPath() + url);
            return; // Quan trọng: Dùng sendRedirect thì phải return để kết thúc hàm
        } else {
            request.setAttribute("message", "Sai email hoặc mật khẩu!");
        }
        
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/signin.jsp").forward(request, response);
    }
}