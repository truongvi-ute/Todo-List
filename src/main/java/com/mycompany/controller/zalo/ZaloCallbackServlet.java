package com.mycompany.controller.zalo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mycompany.data.UserDB;
import com.mycompany.model.User;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(urlPatterns = {"/zalo-callback"})
public class ZaloCallbackServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String code = request.getParameter("code");
        
        // Nếu người dùng hủy hoặc lỗi
        if (code == null) {
            response.sendRedirect("signin.jsp?message=LoginFailed");
            return;
        }

        try {
            // 1. Đổi Code lấy Token
            String accessToken = getAccessToken(code);
            
            // 2. Dùng Token lấy thông tin User (Tên, ID, Avatar)
            JsonObject profile = getUserProfile(accessToken);
            String zaloId = profile.get("id").getAsString();
            String name = profile.get("name").getAsString();
            
            // 3. Kiểm tra DB: Có user này chưa?
            // (Bạn cần thêm hàm selectUserByZaloId vào UserDB như bài trước đã chỉ)
            User user = UserDB.selectUserByZaloId(zaloId);
            
            if (user == null) {
                // Chưa có -> Tạo mới (Đăng ký nhanh)
                user = new User();
                user.setZaloId(zaloId);
                user.setZaloName(name);
                user.setEmail(zaloId + "@zalo.me"); // Email giả định
                user.setPassword("ZALO"); // Mật khẩu giả
                UserDB.insert(user);
            }
            
            // 4. Đăng nhập thành công (Lưu vào Session)
            HttpSession session = request.getSession();
            session.setAttribute("loginedUser", user);
            
            // Về trang chủ
            response.sendRedirect("home.jsp");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("signin.jsp?message=SystemError");
        }
    }

    // --- HÀM GỌI API ZALO (Copy y nguyên) ---
    private String getAccessToken(String code) throws Exception {
        String body = "app_id=" + ZaloConfig.APP_ID + "&app_secret=" + ZaloConfig.APP_SECRET + "&code=" + code;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth.zaloapp.com/v4/access_token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("secret_key", ZaloConfig.APP_SECRET)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
        return json.get("access_token").getAsString();
    }

    private JsonObject getUserProfile(String token) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://graph.zalo.me/v2.0/me?fields=id,name,picture"))
                .header("access_token", token)
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return JsonParser.parseString(resp.body()).getAsJsonObject();
    }
}