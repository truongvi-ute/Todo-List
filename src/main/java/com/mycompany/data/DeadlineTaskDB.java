package com.mycompany.data;

import com.mycompany.model.DeadlineTask;
import com.mycompany.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object (DAO) cho entity DeadlineTask.
 * Cung cấp các phương thức CRUD và truy vấn cho deadline tasks.
 */
public class DeadlineTaskDB {

    /**
     * Thêm task mới vào database.
     * 
     * @param task DeadlineTask object cần lưu
     */
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

    /**
     * Tìm task theo ID.
     * 
     * @param id ID của task
     * @return DeadlineTask nếu tìm thấy, null nếu không tồn tại
     */
    public static DeadlineTask findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(DeadlineTask.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật thông tin task.
     * 
     * @param task DeadlineTask object đã được chỉnh sửa
     */
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

    /**
     * Xóa task khỏi database.
     * 
     * @param id ID của task cần xóa
     */
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

    /**
     * Lấy danh sách tasks của user trong khoảng thời gian.
     * Sắp xếp theo thời gian tạo (mới nhất lên trên).
     * 
     * @param user User sở hữu tasks
     * @param startDate Ngày bắt đầu (inclusive)
     * @param endDate Ngày kết thúc (exclusive)
     * @return Danh sách DeadlineTask
     */
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

    /**
     * Lấy danh sách tasks với filter theo status.
     * Sắp xếp theo thời gian tạo (mới nhất lên trên).
     * 
     * @param user User sở hữu tasks
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param statusFilter Filter status: "all", "IN_PROGRESS", "DONE", "LATE"
     * @return Danh sách DeadlineTask đã lọc
     */
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

    /**
     * Lấy danh sách tasks sắp xếp theo độ ưu tiên.
     * Thứ tự: HIGH > MEDIUM > LOW, sau đó theo thời gian tạo mới nhất.
     * 
     * @param user User sở hữu tasks
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách DeadlineTask đã sắp xếp theo priority
     */
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
                    return p1 - p2;
                }
                return t2.getCreatedAt().compareTo(t1.getCreatedAt());
            });
            
            return tasks;
        } finally {
            em.close();
        }
    }
    
    /**
     * Chuyển đổi Priority enum thành số để sắp xếp.
     * 
     * @param priority Priority enum
     * @return Số thứ tự (1=HIGH, 2=MEDIUM, 3=LOW, 4=null)
     */
    private static int getPriorityOrder(com.mycompany.model.Priority priority) {
        if (priority == null) return 4;
        switch (priority) {
            case HIGH: return 1;
            case MEDIUM: return 2;
            case LOW: return 3;
            default: return 4;
        }
    }

    /**
     * Lấy tasks của user theo ngày với tùy chọn sắp xếp.
     * 
     * @param user User sở hữu tasks
     * @param startOfDay Đầu ngày
     * @param endOfDay Cuối ngày
     * @param sort Kiểu sắp xếp: "priority" hoặc "date"
     * @return Danh sách DeadlineTask
     */
    public static List<DeadlineTask> getTasksByUserAndDate(User user, LocalDateTime startOfDay, 
            LocalDateTime endOfDay, String sort) {
        if ("priority".equals(sort)) {
            return getTasksByUserDateRangeSortByPriority(user, startOfDay, endOfDay);
        } else {
            return getTasksByUserAndDateRange(user, startOfDay, endOfDay);
        }
    }
    
    /**
     * Lấy tasks với cả filter và sort.
     * 
     * @param user User sở hữu tasks
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param filter Filter status: "all", "in_progress", "done", "late"
     * @param sort Kiểu sắp xếp: "priority" hoặc "date"
     * @return Danh sách DeadlineTask đã lọc và sắp xếp
     */
    public static List<DeadlineTask> getTasksWithFilterAndSort(User user, LocalDateTime startDate, 
            LocalDateTime endDate, String filter, String sort) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT t FROM DeadlineTask t WHERE t.user = :user " +
                          "AND t.dueDate >= :startDate AND t.dueDate < :endDate ";
            
            // Add filter condition
            if (!"all".equals(filter) && filter != null) {
                jpql += "AND t.status = :status ";
            }
            
            TypedQuery<DeadlineTask> query = em.createQuery(jpql, DeadlineTask.class);
            query.setParameter("user", user);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            
            if (!"all".equals(filter) && filter != null) {
                query.setParameter("status", com.mycompany.model.Status.valueOf(filter.toUpperCase()));
            }
            
            List<DeadlineTask> tasks = query.getResultList();
            
            // Sort in Java
            if ("priority".equals(sort)) {
                tasks.sort((t1, t2) -> {
                    int p1 = getPriorityOrder(t1.getPriority());
                    int p2 = getPriorityOrder(t2.getPriority());
                    if (p1 != p2) {
                        return p1 - p2;
                    }
                    return t2.getCreatedAt().compareTo(t1.getCreatedAt());
                });
            } else {
                // Sort by createdAt DESC
                tasks.sort((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()));
            }
            
            return tasks;
        } finally {
            em.close();
        }
    }
}
