package com.mycompany.controller;

import com.mycompany.model.Admin;
import com.mycompany.model.User;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Filter kiểm tra xác thực cho các trang JSP.
 * Chặn truy cập vào các trang nội bộ nếu chưa đăng nhập.
 * Áp dụng cho tất cả file *.jsp
 */
@WebFilter(urlPatterns = {"*.jsp"}) 
public class AuthFilter implements Filter {

    private static final String[] DAY_NAMES = {"", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

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
        
        // Set current date for header
        LocalDate today = LocalDate.now();
        String dayName = DAY_NAMES[today.getDayOfWeek().getValue()];
        String formattedDate = dayName + ", " + today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        httpRequest.setAttribute("currentDateFormatted", formattedDate);
        
        // Lấy đường dẫn user đang cố truy cập
        String requestURI = httpRequest.getRequestURI();
        
        // Danh sách các trang được phép truy cập không cần đăng nhập
        boolean isLoginJsp = requestURI.endsWith("signin.jsp");
        boolean isSignupJsp = requestURI.endsWith("signup.jsp");
        boolean isForgotPasswordJsp = requestURI.endsWith("forgot-password.jsp");
        boolean isVerifyOtpJsp = requestURI.endsWith("verify-otp.jsp");
        boolean isResetPasswordJsp = requestURI.endsWith("reset-password.jsp");
        boolean isLoginServlet = requestURI.endsWith("login");
        boolean isLogoutServlet = requestURI.endsWith("logout");
        boolean isCSS = requestURI.endsWith(".css");
        boolean isAdminJsp = requestURI.endsWith("admin.jsp");

        // Kiểm tra trạng thái đăng nhập
        HttpSession session = httpRequest.getSession(false);
        boolean isUserLoggedIn = (session != null && session.getAttribute("loginedUser") != null);
        boolean isAdminLoggedIn = (session != null && session.getAttribute("loginedAdmin") != null);
        
        // Kiểm tra user có bị block không
        if (isUserLoggedIn) {
            User user = (User) session.getAttribute("loginedUser");
            if (user.getIsBlocked()) {
                session.invalidate();
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
                return;
            }
            
            // Chặn user thường truy cập trang admin
            if (isAdminJsp) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/dashboard");
                return;
            }
        }
        
        // Admin chỉ được truy cập trang admin
        if (isAdminLoggedIn && !isAdminJsp) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin");
            return;
        }

        // Logic chặn/cho phép
        if (isUserLoggedIn || isAdminLoggedIn || isLoginJsp || isSignupJsp || isForgotPasswordJsp || isVerifyOtpJsp || isResetPasswordJsp || isLoginServlet || isLogoutServlet || isCSS) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
        }
    }
}
