package com.mycompany.controller;

import com.mycompany.data.UserDB;
import com.mycompany.service.PasswordUtil;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet xử lý đặt lại mật khẩu mới.
 * URL: /reset-password
 * Yêu cầu: Phải verify OTP trước khi truy cập.
 */
@WebServlet(urlPatterns = {"/reset-password"})
public class ResetPasswordServlet extends HttpServlet {

    /**
     * Xử lý POST request - Đặt mật khẩu mới.
     * Validate password, hash và lưu vào database.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        
        String email = (String) session.getAttribute("pendingEmail");
        Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");
        
        // Kiểm tra session hợp lệ
        if (email == null) {
            response.sendRedirect("login");
            return;
        }
        if (otpVerified == null || !otpVerified) {
            response.sendRedirect("verify-otp");
            return;
        }
        
        String newPassword = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String url = "/reset-password.jsp";
        
        // Validate password mới
        if (newPassword.length() < 6) {
            request.setAttribute("message", "Password must be at least 6 characters!");
        } else if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("message", "Passwords do not match!");
        } else {
            // Hash password mới và cập nhật (email đã được verify qua OTP)
            String hashedPassword = PasswordUtil.hash(newPassword);
            UserDB.updatePassword(email, hashedPassword);
            
            // Xóa session tạm
            session.removeAttribute("pendingEmail");
            session.removeAttribute("otpType");
            session.removeAttribute("otpVerified");
            
            // Redirect để URL hiển thị đúng
            session.setAttribute("message", "Password reset successful! Please sign in.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
    
    /**
     * Xử lý GET request - Hiển thị form đặt password mới.
     * Redirect về verify-otp nếu chưa verify, về login nếu không có pending email.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("pendingEmail");
        Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");
        
        if (email == null) {
            response.sendRedirect("login");
            return;
        }
        if (otpVerified == null || !otpVerified) {
            response.sendRedirect("verify-otp");
            return;
        }
        getServletContext().getRequestDispatcher("/reset-password.jsp").forward(request, response);
    }
}
