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

/**
 * Servlet xử lý yêu cầu quên mật khẩu.
 * URL: /forgot-password
 * Flow: Nhập email -> Gửi OTP -> Verify OTP -> Reset password
 */
@WebServlet(urlPatterns = {"/forgot-password"})
public class ForgotPasswordServlet extends HttpServlet {

    /**
     * Xử lý POST request - Gửi OTP reset password.
     * Kiểm tra email tồn tại, gửi OTP nếu hợp lệ.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String email = request.getParameter("email");
        String url = "/forgot-password.jsp";
        
        // Kiểm tra email có tồn tại trong hệ thống
        if (!UserDB.emailExists(email)) {
            request.setAttribute("message", "This email is not registered!");
        } else {
            // Tạo và gửi OTP
            String otp = OtpService.generateOtp();
            OtpService.saveOtp(email, otp);
            
            boolean sent = EmailService.sendPasswordResetEmail(email, otp);
            
            if (sent) {
                // Lưu thông tin vào session
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
    
    /**
     * Xử lý GET request - Hiển thị form nhập email.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/forgot-password.jsp").forward(request, response);
    }
}
