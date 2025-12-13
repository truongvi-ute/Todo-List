package com.mycompany.controller;

import com.mycompany.data.UserDB;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.mycompany.model.User;

@WebServlet(urlPatterns = {"/register"}) // Đường dẫn ảo
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        // 1. Lấy dữ liệu từ form
        String email = request.getParameter("email");
        String pass = request.getParameter("password");
        String confirmPass = request.getParameter("confirmPassword");
        
        String url = "/signup.jsp"; // Mặc định quay lại trang đăng ký nếu lỗi
        
        // 2. Validate (Kiểm tra dữ liệu)
        if (UserDB.emailExists(email)) {
            request.setAttribute("message", "Email này đã được sử dụng!");
        } else if (!pass.equals(confirmPass)) {
            request.setAttribute("message", "Mật khẩu xác nhận không khớp!");
        } else {
            // 3. Nếu ổn -> Tạo user và lưu xuống DB
            User newUser = new User(email, pass);
            UserDB.insert(newUser);
            
            // 4. Chuyển hướng sang trang login
            url = "/signin.jsp";
            request.setAttribute("message", "Đăng ký thành công! Hãy đăng nhập.");
        }
        
        // Gửi dữ liệu message về lại trang JSP
        getServletContext().getRequestDispatcher(url).forward(request, response);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().getRequestDispatcher("/signup.jsp").forward(request, response);
    }
}