package com.mycompany.data;

import com.mycompany.model.DeadlineTask;
import com.mycompany.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

public class DeadlineTaskDB {

    // Thêm task mới
    public static void insert(DeadlineTask task) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(task);
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

    // Tìm task theo ID
    public static DeadlineTask findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(DeadlineTask.class, id);
        } finally {
            em.close();
        }
    }

    // Cập nhật task
    public static void update(DeadlineTask task) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(task);
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


    // Xóa task
    public static void delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            DeadlineTask task = em.find(DeadlineTask.class, id);
            if (task != null) {
                em.remove(task);
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

    // Lấy tasks của user trong khoảng thời gian, sắp xếp theo thời gian tạo (mới nhất lên trên)
    public static List<DeadlineTask> getTasksByUserAndDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT t FROM DeadlineTask t WHERE t.user = :user " +
                          "AND t.dueDate >= :startDate AND t.dueDate < :endDate " +
                          "ORDER BY t.createdAt DESC";
            TypedQuery<DeadlineTask> query = em.createQuery(jpql, DeadlineTask.class);
            query.setParameter("user", user);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Lấy tasks với filter status, sắp xếp theo thời gian tạo (mới nhất lên trên)
    public static List<DeadlineTask> getTasksByUserDateRangeAndStatus(User user, LocalDateTime startDate, 
            LocalDateTime endDate, String statusFilter) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT t FROM DeadlineTask t WHERE t.user = :user " +
                          "AND t.dueDate >= :startDate AND t.dueDate < :endDate ";
            
            if (!"all".equals(statusFilter)) {
                jpql += "AND t.status = :status ";
            }
            jpql += "ORDER BY t.createdAt DESC";
            
            TypedQuery<DeadlineTask> query = em.createQuery(jpql, DeadlineTask.class);
            query.setParameter("user", user);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            
            if (!"all".equals(statusFilter)) {
                query.setParameter("status", com.mycompany.model.Status.valueOf(statusFilter.toUpperCase()));
            }
            
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Lấy tasks sắp xếp theo priority (HIGH > MEDIUM > LOW), sau đó theo thời gian tạo mới nhất
    public static List<DeadlineTask> getTasksByUserDateRangeSortByPriority(User user, LocalDateTime startDate, 
            LocalDateTime endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT t FROM DeadlineTask t WHERE t.user = :user " +
                          "AND t.dueDate >= :startDate AND t.dueDate < :endDate";
            TypedQuery<DeadlineTask> query = em.createQuery(jpql, DeadlineTask.class);
            query.setParameter("user", user);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            
            List<DeadlineTask> tasks = query.getResultList();
            
            // Sort trong Java: HIGH(1) > MEDIUM(2) > LOW(3), sau đó createdAt DESC
            tasks.sort((t1, t2) -> {
                int p1 = getPriorityOrder(t1.getPriority());
                int p2 = getPriorityOrder(t2.getPriority());
                if (p1 != p2) {
                    return p1 - p2; // Priority cao hơn lên trước
                }
                return t2.getCreatedAt().compareTo(t1.getCreatedAt()); // Mới hơn lên trước
            });
            
            return tasks;
        } finally {
            em.close();
        }
    }
    
    // Helper: Chuyển Priority thành số để sort
    private static int getPriorityOrder(com.mycompany.model.Priority priority) {
        if (priority == null) return 4;
        switch (priority) {
            case HIGH: return 1;
            case MEDIUM: return 2;
            case LOW: return 3;
            default: return 4;
        }
    }
}
