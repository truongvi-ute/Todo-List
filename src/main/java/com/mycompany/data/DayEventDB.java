package com.mycompany.data;

import com.mycompany.model.DayEvent;
import com.mycompany.model.DayEventStatus;
import com.mycompany.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Data Access Object (DAO) cho entity DayEvent.
 * Cung cấp các phương thức CRUD và truy vấn cho day events.
 * Đây là bảng chính để query và hiển thị lên giao diện Lịch.
 */
public class DayEventDB {

    /**
     * Thêm DayEvent mới vào database.
     * 
     * @param dayEvent DayEvent object cần lưu
     */
    public static void insert(DayEvent dayEvent) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(dayEvent);
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
     * Tìm DayEvent theo ID.
     * 
     * @param id ID của DayEvent
     * @return DayEvent nếu tìm thấy, null nếu không tồn tại
     */
    public static DayEvent findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(DayEvent.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật thông tin DayEvent.
     * 
     * @param dayEvent DayEvent object đã được chỉnh sửa
     */
    public static void update(DayEvent dayEvent) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(dayEvent);
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
     * Xóa DayEvent khỏi database.
     * 
     * @param id ID của DayEvent cần xóa
     */
    public static void delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            DayEvent dayEvent = em.find(DayEvent.class, id);
            if (dayEvent != null) {
                em.remove(dayEvent);
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
     * Lấy danh sách DayEvents của user trong một ngày cụ thể.
     * Chỉ lấy các events có status = ACTIVE.
     * Sắp xếp theo giờ bắt đầu.
     * 
     * @param user User sở hữu events
     * @param date Ngày cần lấy
     * @return Danh sách DayEvent đã sắp xếp
     */
    public static List<DayEvent> getByUserAndDate(User user, LocalDate date) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT d FROM DayEvent d " +
                          "JOIN FETCH d.scheduleEvent e " +
                          "WHERE e.user = :user " +
                          "AND d.specificDate = :date " +
                          "AND d.status = :status " +
                          "ORDER BY COALESCE(d.overrideStartTime, e.defaultStartTime) ASC";
            TypedQuery<DayEvent> query = em.createQuery(jpql, DayEvent.class);
            query.setParameter("user", user);
            query.setParameter("date", date);
            query.setParameter("status", DayEventStatus.ACTIVE);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy danh sách DayEvents của user trong khoảng thời gian.
     * Bao gồm cả ACTIVE và CANCELLED để hiển thị trên lịch.
     * 
     * @param user User sở hữu events
     * @param startDate Ngày bắt đầu (inclusive)
     * @param endDate Ngày kết thúc (inclusive)
     * @return Danh sách DayEvent
     */
    public static List<DayEvent> getByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT d FROM DayEvent d " +
                          "JOIN FETCH d.scheduleEvent e " +
                          "WHERE e.user = :user " +
                          "AND d.specificDate >= :startDate " +
                          "AND d.specificDate <= :endDate " +
                          "ORDER BY d.specificDate ASC, COALESCE(d.overrideStartTime, e.defaultStartTime) ASC";
            TypedQuery<DayEvent> query = em.createQuery(jpql, DayEvent.class);
            query.setParameter("user", user);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả DayEvents (bao gồm cả CANCELLED) của một ScheduleEvent.
     * 
     * @param scheduleEventId ID của ScheduleEvent cha
     * @return Danh sách DayEvent
     */
    public static List<DayEvent> getByScheduleEventId(Long scheduleEventId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT d FROM DayEvent d " +
                          "WHERE d.scheduleEvent.id = :eventId " +
                          "ORDER BY d.specificDate ASC";
            TypedQuery<DayEvent> query = em.createQuery(jpql, DayEvent.class);
            query.setParameter("eventId", scheduleEventId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Hủy một buổi cụ thể (set status = CANCELLED).
     * 
     * @param dayEventId ID của DayEvent cần hủy
     */
    public static void cancel(Long dayEventId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            DayEvent dayEvent = em.find(DayEvent.class, dayEventId);
            if (dayEvent != null) {
                dayEvent.setStatus(DayEventStatus.CANCELLED);
                em.merge(dayEvent);
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
     * Khôi phục một buổi đã hủy (set status = ACTIVE).
     * 
     * @param dayEventId ID của DayEvent cần khôi phục
     */
    public static void restore(Long dayEventId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            DayEvent dayEvent = em.find(DayEvent.class, dayEventId);
            if (dayEvent != null) {
                dayEvent.setStatus(DayEventStatus.ACTIVE);
                em.merge(dayEvent);
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
     * Ghi đè giờ cho một buổi cụ thể.
     * 
     * @param dayEventId ID của DayEvent
     * @param newStartTime Giờ bắt đầu mới (null để dùng giờ mặc định)
     * @param newEndTime Giờ kết thúc mới (null để dùng giờ mặc định)
     */
    public static void overrideTime(Long dayEventId, LocalTime newStartTime, LocalTime newEndTime) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            DayEvent dayEvent = em.find(DayEvent.class, dayEventId);
            if (dayEvent != null) {
                dayEvent.setOverrideStartTime(newStartTime);
                dayEvent.setOverrideEndTime(newEndTime);
                em.merge(dayEvent);
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
     * Đếm số buổi ACTIVE của một ScheduleEvent.
     * 
     * @param scheduleEventId ID của ScheduleEvent
     * @return Số buổi active
     */
    public static long countActiveByScheduleEvent(Long scheduleEventId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT COUNT(d) FROM DayEvent d " +
                          "WHERE d.scheduleEvent.id = :eventId " +
                          "AND d.status = :status";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("eventId", scheduleEventId);
            query.setParameter("status", DayEventStatus.ACTIVE);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * Tìm DayEvent theo ScheduleEvent và ngày cụ thể.
     * 
     * @param scheduleEventId ID của ScheduleEvent
     * @param date Ngày cần tìm
     * @return DayEvent nếu tìm thấy, null nếu không
     */
    public static DayEvent findByScheduleEventAndDate(Long scheduleEventId, LocalDate date) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT d FROM DayEvent d " +
                          "WHERE d.scheduleEvent.id = :eventId " +
                          "AND d.specificDate = :date";
            TypedQuery<DayEvent> query = em.createQuery(jpql, DayEvent.class);
            query.setParameter("eventId", scheduleEventId);
            query.setParameter("date", date);
            List<DayEvent> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }
}
