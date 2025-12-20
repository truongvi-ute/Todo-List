package com.mycompany.controller;

import com.mycompany.data.UserDB;
import com.mycompany.model.User;
import com.mycompany.service.OtpService;
import com.mycompany.service.PasswordUtil;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet xử lý xác thực mã OTP.
 * URL: /verify-otp
 * Dùng cho cả đăng ký (register) và quên mật khẩu (reset).
 */
@WebServlet(urlPatterns = {"/verify-otp"})
public class VerifyOtpServlet extends HttpServlet {

    /**
     * Xử lý POST request - Xác thực OTP.
     * Nếu OTP hợp lệ: tạo user mới (register) hoặc cho phép reset password.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        
        String otp = request.getParameter("otp");
        String email = (String) session.getAttribute("pendingEmail");
        String otpType = (String) session.getAttribute("otpType");
        
        // Kiểm tra session hợp lệ
        if (email == null || otpType == null) {
            response.sendRedirect("login");
            return;
        }
        
        String url = "/verify-otp.jsp";
        
        // Xác thực OTP
        if (OtpService.verifyOtp(email, otp)) {
            if ("register".equals(otpType)) {
                // Đăng ký: Tạo user mới với password đã hash
                String password = (String) session.getAttribute("pendingPassword");
                String hashedPassword = PasswordUtil.hash(password);
                User newUser = new User(email, hashedPassword);
                UserDB.insert(newUser);
                
                // Xóa session tạm
                session.removeAttribute("pendingEmail");
                session.removeAttribute("pendingPassword");
                session.removeAttribute("otpType");
                
                url = "/signin.jsp";
                request.setAttribute("message", "Registration successful! Please sign in.");
            } else if ("reset".equals(otpType)) {
                // Reset password: Đánh dấu đã verify, cho phép đặt password mới
                session.setAttribute("otpVerified", true);
                url = "/reset-password.jsp";
            }
        } else {
            request.setAttribute("message", "Invalid or expired OTP code!");
        }
        
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
    
    /**
     * Xử lý GET request - Hiển thị form nhập OTP.
     * Redirect về login nếu không có pending email.
     */
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
