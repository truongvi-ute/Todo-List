package com.mycompany.controller.zalo;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/zalo-login"})
public class ZaloLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Tạo mã bảo mật ngẫu nhiên
        String state = UUID.randomUUID().toString();
        request.getSession().setAttribute("ZALO_STATE", state);
        
        // Tạo đường dẫn gửi sang Zalo
        String loginUrl = "https://oauth.zaloapp.com/v4/permission"
                + "?app_id=" + ZaloConfig.APP_ID
                + "&redirect_uri=" + ZaloConfig.REDIRECT_URI
                + "&state=" + state;
        
        response.sendRedirect(loginUrl);
    }
}