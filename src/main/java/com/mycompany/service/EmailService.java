package com.mycompany.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class EmailService {

    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String API_KEY = System.getenv("BREVO_API_KEY");
    private static final String SENDER_EMAIL = "nguyendoantruongvi11@gmail.com";
    private static final String SENDER_NAME = "TodoList App";

    public static boolean sendOtpEmail(String toEmail, String otpCode) {
        String subject = "OTP Verification - TodoList";
        String body = buildOtpEmailContent(otpCode);
        return sendEmail(toEmail, subject, body);
    }

    public static boolean sendPasswordResetEmail(String toEmail, String otpCode) {
        String subject = "Password Reset - TodoList";
        String body = buildResetPasswordEmailContent(otpCode);
        return sendEmail(toEmail, subject, body);
    }
    
    public static boolean sendDailyReminder(String toEmail, String htmlContent) {
        String subject = "Daily Reminder - TodoList";
        return sendEmail(toEmail, subject, htmlContent);
    }

    private static boolean sendEmail(String toEmail, String subject, String bodyHtml) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "application/json");
            conn.setRequestProperty("api-key", API_KEY);
            conn.setRequestProperty("content-type", "application/json");
            conn.setDoOutput(true);

            String safeBody = bodyHtml.replace("\"", "\\\"")
                    .replace("\n", "")
                    .replace("\r", "");

            String jsonInputString = "{"
                    + "\"sender\":{\"name\":\"" + SENDER_NAME + "\",\"email\":\"" + SENDER_EMAIL + "\"},"
                    + "\"to\":[{\"email\":\"" + toEmail + "\"}],"
                    + "\"subject\":\"" + subject + "\","
                    + "\"htmlContent\":\"" + safeBody + "\""
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 201 || responseCode == 200) {
                System.out.println("Email sent successfully!");
                return true;
            } else {
                System.out.println("Failed to send email. Response Code: " + responseCode);
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println("Error details: " + response.toString());
                }
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending email: " + e.getMessage());
            return false;
        }
    }


    private static String buildOtpEmailContent(String otpCode) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                + "<h2 style='color: #6a11cb;'>Verify Your TodoList Account</h2>"
                + "<p>Hello,</p>"
                + "<p>Your OTP verification code is:</p>"
                + "<div style='background: linear-gradient(135deg, #6a11cb, #2575fc); color: white; "
                + "font-size: 32px; font-weight: bold; text-align: center; "
                + "padding: 20px; border-radius: 10px; letter-spacing: 8px;'>"
                + otpCode
                + "</div>"
                + "<p style='margin-top: 20px;'>This code is valid for <strong>10 minutes</strong>.</p>"
                + "<p>If you did not request this code, please ignore this email.</p>"
                + "<hr style='margin: 30px 0; border: none; border-top: 1px solid #eee;'>"
                + "<p style='color: #999; font-size: 12px;'>TodoList App</p>"
                + "</div>";
    }

    private static String buildResetPasswordEmailContent(String otpCode) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>"
                + "<h2 style='color: #6a11cb;'>Reset Your TodoList Password</h2>"
                + "<p>Hello,</p>"
                + "<p>You have requested to reset your password. Your verification code is:</p>"
                + "<div style='background: linear-gradient(135deg, #6a11cb, #2575fc); color: white; "
                + "font-size: 32px; font-weight: bold; text-align: center; "
                + "padding: 20px; border-radius: 10px; letter-spacing: 8px;'>"
                + otpCode
                + "</div>"
                + "<p style='margin-top: 20px;'>This code is valid for <strong>10 minutes</strong>.</p>"
                + "<p>If you did not request a password reset, please ignore this email.</p>"
                + "<hr style='margin: 30px 0; border: none; border-top: 1px solid #eee;'>"
                + "<p style='color: #999; font-size: 12px;'>TodoList App</p>"
                + "</div>";
    }
}
