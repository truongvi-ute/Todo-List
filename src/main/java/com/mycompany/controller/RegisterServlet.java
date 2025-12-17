package com.mycompany.controller;

import com.mycompany.data.UserDB;
import com.mycompany.service.EmailService;
import com.mycompany.service.OtpService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.mycompany.model.User;

@WebServlet(urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String email = request.getParameter("email");
        String pass = request.getParameter("password");
        String confirmPass = request.getParameter("confirmPassword");
        
        String url = "/signup.jsp";
        
        // Validate
        if (UserDB.emailExists(email)) {
            request.setAttribute("message", "This email is already registered!");
        } else if (!pass.equals(confirmPass)) {
            request.setAttribute("message", "Passwords do not match!");
        } else if (pass.length() < 6) {
            request.setAttribute("message", "Password must be at least 6 characters!");
        } else {
            String otp = OtpService.generateOtp();
            OtpService.saveOtp(email, otp);
            
            boolean sent = EmailService.sendOtpEmail(email, otp);
            
            if (sent) {
                HttpSession session = request.getSession();
                session.setAttribute("pendingEmail", email);
                session.setAttribute("pendingPassword", pass);
                session.setAttribute("otpType", "register");
                
                url = "/verify-otp.jsp";
                request.setAttribute("message", "OTP code has been sent to your email!");
            } else {
                request.setAttribute("message", "Failed to send email. Please try again!");
            }
        }
        
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/signup.jsp").forward(request, response);
    }
}