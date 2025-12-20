package com.mycompany.service;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class để mã hóa và xác thực mật khẩu sử dụng BCrypt.
 * BCrypt là thuật toán hash an toàn, tự động thêm salt và có độ phức tạp có thể điều chỉnh.
 */
public class PasswordUtil {
    
    /**
     * Mã hóa mật khẩu plaintext thành chuỗi hash BCrypt.
     * 
     * @param password Mật khẩu gốc cần mã hóa
     * @return Chuỗi hash BCrypt (bao gồm salt và cost factor)
     */
    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
    
    /**
     * Xác thực mật khẩu plaintext với chuỗi hash đã lưu.
     * 
     * @param password Mật khẩu người dùng nhập vào
     * @param hashedPassword Chuỗi hash đã lưu trong database
     * @return true nếu mật khẩu khớp, false nếu không khớp
     */
    public static boolean verify(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
