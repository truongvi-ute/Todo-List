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

/**
 * Filter kiểm tra xác thực cho các trang JSP.
 * Chặn truy cập vào các trang nội bộ nếu chưa đăng nhập.
 * Áp dụng cho tất cả file *.jsp
 */
@WebFilter(urlPatterns = {"*.jsp"}) 
public class AuthFilter implements Filter {

    /**
     * Kiểm tra xác thực trước khi cho phép truy cập trang JSP.
     * Cho phép truy cập: signin.jsp, signup.jsp, các trang OTP, CSS files.
     * Các trang khác yêu cầu đăng nhập.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Lấy đường dẫn user đang cố truy cập
        String requestURI = httpRequest.getRequestURI();
        
        // Danh sách các trang được phép truy cập không cần đăng nhập
        boolean isLoginJsp = requestURI.endsWith("signin.jsp");
        boolean isSignupJsp = requestURI.endsWith("signup.jsp");
        boolean isLoginServlet = requestURI.endsWith("login");
        boolean isLogoutServlet = requestURI.endsWith("logout");
        boolean isCSS = requestURI.endsWith(".css");

        // Kiểm tra trạng thái đăng nhập
        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("loginedUser") != null);

        // Logic chặn/cho phép
        if (isLoggedIn || isLoginJsp || isSignupJsp || isLoginServlet || isLogoutServlet || isCSS) {
            chain.doFilter(request, response); // Cho phép truy cập
        } else {
            // Chưa đăng nhập -> Redirect về trang login
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
        }
    }
}
