package com.mycompany.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import com.mycompany.model.User;

/**
 * Data Access Object (DAO) cho entity User.
 * Cung cấp các phương thức CRUD và truy vấn liên quan đến User.
 */
public class UserDB {

    /**
     * Thêm User mới vào database (Đăng ký tài khoản).
     * 
     * @param user User object cần lưu
     */
    public static void insert(User user) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm User theo email.
     * Dùng cho đăng nhập và kiểm tra email trùng lặp.
     * 
     * @param email Email cần tìm
     * @return User nếu tìm thấy, null nếu không tồn tại
     */
    public static User selectUser(String email) {
        EntityManager em = JPAUtil.getEntityManager();
        String qString = "SELECT u FROM User u WHERE u.email = :email";
        TypedQuery<User> q = em.createQuery(qString, User.class);
        q.setParameter("email", email);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
    
    /**
     * Kiểm tra nhanh email đã tồn tại trong hệ thống chưa.
     * 
     * @param email Email cần kiểm tra
     * @return true nếu email đã tồn tại, false nếu chưa
     */
    public static boolean emailExists(String email) {
        return selectUser(email) != null;
    }
    
    /**
     * Cập nhật mật khẩu của User.
     * Dùng cho chức năng đổi mật khẩu và reset password.
     * 
     * @param email Email của User cần cập nhật
     * @param newPassword Mật khẩu mới (đã được hash)
     */
    public static void updatePassword(String email, String newPassword) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            User user = selectUser(email);
            if (user != null) {
                user = em.merge(user);
                user.setPassword(newPassword);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
    
    /**
     * Cập nhật cài đặt thông báo email hàng ngày.
     * 
     * @param email Email của User
     * @param enabled Bật/tắt thông báo
     * @param hour Giờ gửi thông báo (0-23)
     */
    public static void updateNotificationSettings(String email, boolean enabled, int hour) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            User user = selectUser(email);
            if (user != null) {
                user = em.merge(user);
                user.setNotificationEnabled(enabled);
                user.setNotificationHour(hour);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
    
    /**
     * Lấy danh sách Users có bật thông báo tại giờ cụ thể.
     * Dùng cho cron job gửi daily reminder.
     * 
     * @param hour Giờ cần lấy (0-23)
     * @return Danh sách Users có notification_hour = hour và notification_enabled = true
     */
    public static java.util.List<User> getUsersWithNotificationAt(int hour) {
        EntityManager em = JPAUtil.getEntityManager();
        String qString = "SELECT u FROM User u WHERE u.notificationEnabled = true AND u.notificationHour = :hour";
        TypedQuery<User> q = em.createQuery(qString, User.class);
        q.setParameter("hour", hour);
        try {
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
