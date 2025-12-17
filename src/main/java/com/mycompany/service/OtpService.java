package com.mycompany.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OtpService {
    
    // Lưu OTP trong memory (email -> OtpData)
    private static final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    
    // Tạo mã OTP ngẫu nhiên
    public static String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    
    // Lưu OTP cho email
    public static void saveOtp(String email, String otp) {
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
        otpStorage.put(email.toLowerCase(), new OtpData(otp, expiry));
    }
    
    // Xác thực OTP
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
    
    // Xóa OTP
    public static void removeOtp(String email) {
        otpStorage.remove(email.toLowerCase());
    }
    
    // Inner class lưu dữ liệu OTP
    private static class OtpData {
        String otp;
        LocalDateTime expiry;
        
        OtpData(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}
