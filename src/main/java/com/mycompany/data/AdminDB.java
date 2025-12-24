package com.mycompany.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import com.mycompany.model.Admin;

/**
 * Data Access Object (DAO) cho entity Admin.
 */
public class AdminDB {

    /**
     * Tìm Admin theo email.
     * 
     * @param email Email cần tìm
     * @return Admin nếu tìm thấy, null nếu không tồn tại
     */
    public static Admin selectAdmin(String email) {
        EntityManager em = JPAUtil.getEntityManager();
        String qString = "SELECT a FROM Admin a WHERE a.email = :email";
        TypedQuery<Admin> q = em.createQuery(qString, Admin.class);
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
     * Thêm Admin mới vào database.
     * 
     * @param admin Admin object cần lưu
     */
    public static void insert(Admin admin) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(admin);
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
}
