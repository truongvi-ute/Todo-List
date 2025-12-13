package com.mycompany.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import com.mycompany.model.User;

public class UserDB {

    // Hàm thêm User mới (Đăng ký)
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

    // Hàm tìm User theo Email (Dùng cho Đăng nhập và kiểm tra trùng lặp)
    public static User selectUser(String email) {
        EntityManager em = JPAUtil.getEntityManager();
        String qString = "SELECT u FROM User u WHERE u.email = :email";
        TypedQuery<User> q = em.createQuery(qString, User.class);
        q.setParameter("email", email);
        try {
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null; // Không tìm thấy user
        } finally {
            em.close();
        }
    }
    
    // Hàm kiểm tra nhanh email đã tồn tại chưa
    public static boolean emailExists(String email) {
        return selectUser(email) != null;
    }
}