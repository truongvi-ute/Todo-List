package com.mycompany.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service quản lý mã OTP (One-Time Password) cho xác thực email.
 * Lưu trữ OTP trong memory với thời gian hết hạn.
 */
public class OtpService {
    
    // Lưu OTP trong memory (email -> OtpData)
    private static final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    
    /**
     * Tạo mã OTP ngẫu nhiên 6 chữ số.
     * Sử dụng SecureRandom để đảm bảo tính ngẫu nhiên an toàn.
     * 
     * @return Chuỗi OTP 6 chữ số
     */
    public static String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    
    /**
     * Lưu OTP cho email với thời gian hết hạn 10 phút.
     * 
     * @param email Email của người dùng
     * @param otp Mã OTP cần lưu
     */
    public static void saveOtp(String email, String otp) {
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        otpStorage.put(email.toLowerCase(), new OtpData(otp, expiry));
    }
    
    /**
     * Xác thực mã OTP người dùng nhập vào.
     * Kiểm tra OTP có tồn tại, chưa hết hạn và khớp với mã đã lưu.
     * OTP sẽ bị xóa sau khi verify thành công.
     * 
     * @param email Email của người dùng
     * @param inputOtp Mã OTP người dùng nhập
     * @return true nếu OTP hợp lệ, false nếu không hợp lệ hoặc hết hạn
     */
    public static boolean verifyOtp(String email, String inputOtp) {
        OtpData data = otpStorage.get(email.toLowerCase());
        
        if (data == null) {
            return false;
        }
        
        // Kiểm tra hết hạn
        if (LocalDateTime.now().isAfter(data.expiry)) {
            otpStorage.remove(email.toLowerCase());
            return false;
        }
        
        // Kiểm tra mã OTP
        if (data.otp.equals(inputOtp)) {
            otpStorage.remove(email.toLowerCase()); // Xóa sau khi verify thành công
            return true;
        }
        
        return false;
    }
    
    /**
     * Xóa OTP của email khỏi storage.
     * 
     * @param email Email cần xóa OTP
     */
    public static void removeOtp(String email) {
        otpStorage.remove(email.toLowerCase());
    }
    
    /**
     * Inner class lưu trữ dữ liệu OTP và thời gian hết hạn.
     */
    private static class OtpData {
        String otp;
        LocalDateTime expiry;
        
        OtpData(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}
