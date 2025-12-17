package com.mycompany.controller;

import com.mycompany.data.UserDB;
import com.mycompany.model.User;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/reset-password"})
public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        
        String email = (String) session.getAttribute("pendingEmail");
        Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");
        
        // Kiểm tra đã verify OTP chưa
        if (email == null || otpVerified == null || !otpVerified) {
            response.sendRedirect("login");
            return;
        }
        
        String newPassword = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String url = "/reset-password.jsp";
        
        if (newPassword.length() < 6) {
            request.setAttribute("message", "Password must be at least 6 characters!");
        } else if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("message", "Passwords do not match!");
        } else {
            User user = UserDB.selectUser(email);
            if (user != null) {
                UserDB.updatePassword(email, newPassword);
                
                session.removeAttribute("pendingEmail");
                session.removeAttribute("otpType");
                session.removeAttribute("otpVerified");
                
                url = "/signin.jsp";
                request.setAttribute("message", "Password reset successful! Please sign in.");
            } else {
                request.setAttribute("message", "An error occurred. Please try again!");
            }
        }
        
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");
        
        if (otpVerified == null || !otpVerified) {
            response.sendRedirect("login");
            return;
        }
        getServletContext().getRequestDispatcher("/reset-password.jsp").forward(request, response);
    }
}
