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

@WebServlet(urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String email = request.getParameter("email");
        String url = "/forgot-password.jsp";
        
        if (!UserDB.emailExists(email)) {
            request.setAttribute("message", "This email is not registered!");
        } else {
            String otp = OtpService.generateOtp();
            OtpService.saveOtp(email, otp);
            
            boolean sent = EmailService.sendPasswordResetEmail(email, otp);
            
            if (sent) {
                HttpSession session = request.getSession();
                session.setAttribute("pendingEmail", email);
                session.setAttribute("otpType", "reset");
                
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
        getServletContext().getRequestDispatcher("/forgot-password.jsp").forward(request, response);
    }
}
