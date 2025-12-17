package com.mycompany.controller;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

// Áp dụng cho toàn bộ các file JSP
@WebFilter(urlPatterns = {"*.jsp"}) 
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Lấy đường dẫn user đang cố truy cập (Ví dụ: /TodoList/home.jsp)
        String requestURI = httpRequest.getRequestURI();
        
        // 1. DANH SÁCH CÁC TRANG ĐƯỢC PHÉP TRUY CẬP KHÔNG CẦN LOGIN
        // Lưu ý: Phải khớp chính xác tên file trong thư mục Web Pages
        boolean isLoginJsp = requestURI.endsWith("signin.jsp");   // Trang đăng nhập
        boolean isSignupJsp = requestURI.endsWith("signup.jsp");  // Trang đăng ký (SỬA LẠI TỪ REGISTER -> SIGNUP)
        boolean isLoginServlet = requestURI.endsWith("login");    // URL xử lý login
        boolean isLogoutServlet = requestURI.endsWith("logout");  // URL xử lý logout
        boolean isCSS = requestURI.endsWith(".css");              // File CSS

        // 2. KIỂM TRA ĐĂNG NHẬP
        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("loginedUser") != null);

        // 3. LOGIC CHẶN/CHO PHÉP
        // Nếu (Đã đăng nhập) HOẶC (Đang vào các trang cho phép ở trên)
        if (isLoggedIn || isLoginJsp || isSignupJsp || isLoginServlet || isLogoutServlet || isCSS) {
            chain.doFilter(request, response); // Cho qua
        } else {
            // Nếu chưa đăng nhập mà cố vào trang nội bộ -> Đá về Login
            // Dùng contextPath để đảm bảo đường dẫn đúng: /TenProject/signin.jsp
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
        }
    }
}