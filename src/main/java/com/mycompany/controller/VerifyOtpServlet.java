package com.mycompany.controller;

import com.mycompany.data.UserDB;
import com.mycompany.model.User;
import com.mycompany.service.OtpService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/verify-otp"})
public class VerifyOtpServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        
        String otp = request.getParameter("otp");
        String email = (String) session.getAttribute("pendingEmail");
        String otpType = (String) session.getAttribute("otpType");
        
        if (email == null || otpType == null) {
            response.sendRedirect("login");
            return;
        }
        
        String url = "/verify-otp.jsp";
        
        if (OtpService.verifyOtp(email, otp)) {
            if ("register".equals(otpType)) {
                // Đăng ký: Tạo user mới
                String password = (String) session.getAttribute("pendingPassword");
                User newUser = new User(email, password);
                UserDB.insert(newUser);
                
                // Xóa session tạm
                session.removeAttribute("pendingEmail");
                session.removeAttribute("pendingPassword");
                session.removeAttribute("otpType");
                
                url = "/signin.jsp";
                request.setAttribute("message", "Registration successful! Please sign in.");
            } else if ("reset".equals(otpType)) {
                session.setAttribute("otpVerified", true);
                url = "/reset-password.jsp";
            }
        } else {
            request.setAttribute("message", "Invalid or expired OTP code!");
        }
        
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        if (session.getAttribute("pendingEmail") == null) {
            response.sendRedirect("login");
            return;
        }
        getServletContext().getRequestDispatcher("/verify-otp.jsp").forward(request, response);
    }
}
