package com.mycompany.data;

import com.mycompany.model.RecurrenceRule;
import com.mycompany.model.ScheduleEvent;
import com.mycompany.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Access Object (DAO) cho entity ScheduleEvent.
 * Cung cấp các phương thức CRUD và truy vấn cho schedule events.
 * Hỗ trợ cả events đơn lẻ và recurring events.
 */
public class ScheduleEventDB {

    /**
     * Thêm event mới vào database.
     * 
     * @param event ScheduleEvent object cần lưu
     */
    public static void insert(ScheduleEvent event) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(event);
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
     * Tìm event theo ID.
     * 
     * @param id ID của event
     * @return ScheduleEvent nếu tìm thấy, null nếu không tồn tại
     */
    public static ScheduleEvent findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(ScheduleEvent.class, id);
        } finally {
            em.close();
        }
    }

    /**
     * Cập nhật thông tin event.
     * 
     * @param event ScheduleEvent object đã được chỉnh sửa
     */
    public static void update(ScheduleEvent event) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(event);
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
     * Xóa event khỏi database.
     * Cascade sẽ tự động xóa RecurrenceRule và modified instances liên quan.
     * 
     * @param id ID của event cần xóa
     */
    public static void delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            ScheduleEvent event = em.find(ScheduleEvent.class, id);
            if (event != null) {
                em.remove(event);
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
     * Lấy danh sách events KHÔNG lặp lại của user trong khoảng thời gian.
     * Sắp xếp theo thời gian bắt đầu.
     * 
     * @param user User sở hữu events
     * @param startDate Ngày bắt đầu (inclusive)
     * @param endDate Ngày kết thúc (exclusive)
     * @return Danh sách ScheduleEvent không có recurrence rule
     */
    public static List<ScheduleEvent> getEventsByUserAndDateRange(User user, LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT e FROM ScheduleEvent e WHERE e.user = :user " +
                          "AND e.startTime >= :startDate AND e.startTime < :endDate " +
                          "AND e.recurrenceRule IS NULL " +
                          "ORDER BY e.startTime ASC";
            TypedQuery<ScheduleEvent> query = em.createQuery(jpql, ScheduleEvent.class);
            query.setParameter("user", user);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy tất cả recurring events của user.
     * Dùng để hiển thị trong dropdown quản lý ngoại lệ.
     * 
     * @param user User sở hữu events
     * @return Danh sách recurring events (có recurrence rule, không phải modified instance)
     */
    public static List<ScheduleEvent> getRecurringEventsByUser(User user) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT e FROM ScheduleEvent e WHERE e.user = :user " +
                          "AND e.recurrenceRule IS NOT NULL " +
                          "AND e.originalEvent IS NULL " +
                          "ORDER BY e.title ASC";
            TypedQuery<ScheduleEvent> query = em.createQuery(jpql, ScheduleEvent.class);
            query.setParameter("user", user);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy recurring events có thể xuất hiện trong khoảng thời gian.
     * Điều kiện: startTime <= endDate AND (untilDate >= startDate OR untilDate IS NULL)
     * 
     * @param user User sở hữu events
     * @param startDate Ngày bắt đầu khoảng thời gian
     * @param endDate Ngày kết thúc khoảng thời gian
     * @return Danh sách recurring events cần expand
     */
    public static List<ScheduleEvent> getRecurringEventsInRange(User user, LocalDate startDate, LocalDate endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT e FROM ScheduleEvent e " +
                          "LEFT JOIN FETCH e.recurrenceRule r " +
                          "WHERE e.user = :user " +
                          "AND e.recurrenceRule IS NOT NULL " +
                          "AND e.originalEvent IS NULL " +
                          "AND e.startTime <= :endDateTime " +
                          "AND (r.untilDate IS NULL OR r.untilDate >= :startDate)";
            TypedQuery<ScheduleEvent> query = em.createQuery(jpql, ScheduleEvent.class);
            query.setParameter("user", user);
            query.setParameter("startDate", startDate);
            query.setParameter("endDateTime", endDate.plusDays(1).atStartOfDay());
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Thêm ngày ngoại lệ vào RecurrenceRule.
     * Event sẽ không xuất hiện vào ngày này.
     * 
     * @param eventId ID của recurring event
     * @param excludedDate Ngày cần loại trừ
     */
    public static void addExcludedDate(Long eventId, LocalDate excludedDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            ScheduleEvent event = em.find(ScheduleEvent.class, eventId);
            if (event != null && event.getRecurrenceRule() != null) {
                RecurrenceRule rule = event.getRecurrenceRule();
                if (!rule.getExcludedDates().contains(excludedDate)) {
                    rule.getExcludedDates().add(excludedDate);
                    em.merge(rule);
                }
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
     * Tạo modified instance cho một ngày cụ thể của recurring event.
     * Dùng khi muốn thay đổi thời gian của một occurrence cụ thể.
     * Ngày gốc sẽ được thêm vào excluded dates.
     * 
     * @param originalEventId ID của recurring event gốc
     * @param date Ngày của occurrence cần modify
     * @param newStartTime Thời gian bắt đầu mới
     * @param newEndTime Thời gian kết thúc mới
     */
    public static void createModifiedInstance(Long originalEventId, LocalDate date, 
            LocalDateTime newStartTime, LocalDateTime newEndTime) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            ScheduleEvent originalEvent = em.find(ScheduleEvent.class, originalEventId);
            if (originalEvent != null && originalEvent.getRecurrenceRule() != null) {
                // Tạo instance mới với thời gian đã thay đổi
                ScheduleEvent modifiedInstance = new ScheduleEvent(
                    originalEvent.getTitle(),
                    originalEvent.getDescription(),
                    newStartTime,
                    newEndTime,
                    originalEvent.getUser()
                );
                modifiedInstance.setOriginalEvent(originalEvent);
                em.persist(modifiedInstance);
                
                // Thêm ngày gốc vào excluded dates để không hiển thị occurrence gốc
                RecurrenceRule rule = originalEvent.getRecurrenceRule();
                if (!rule.getExcludedDates().contains(date)) {
                    rule.getExcludedDates().add(date);
                    em.merge(rule);
                }
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
     * Lấy các modified instances của một recurring event trong khoảng thời gian.
     * 
     * @param originalEventId ID của recurring event gốc
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @return Danh sách modified instances
     */
    public static List<ScheduleEvent> getModifiedInstances(Long originalEventId, LocalDateTime startDate, LocalDateTime endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT e FROM ScheduleEvent e WHERE e.originalEvent.id = :originalId " +
                          "AND e.startTime >= :startDate AND e.startTime < :endDate";
            TypedQuery<ScheduleEvent> query = em.createQuery(jpql, ScheduleEvent.class);
            query.setParameter("originalId", originalEventId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    /**
     * Kiểm tra xem có event nào trùng giờ không.
     * Chỉ kiểm tra events không lặp lại.
     * 
     * @param user User sở hữu events
     * @param startTime Thời gian bắt đầu cần kiểm tra
     * @param endTime Thời gian kết thúc cần kiểm tra
     * @param excludeEventId ID của event cần loại trừ (khi edit), null nếu thêm mới
     * @return true nếu có event trùng giờ, false nếu không
     */
    public static boolean hasOverlappingEvent(User user, LocalDateTime startTime, LocalDateTime endTime, Long excludeEventId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Overlap condition: newStart < existingEnd AND newEnd > existingStart
            String jpql = "SELECT COUNT(e) FROM ScheduleEvent e WHERE e.user = :user " +
                          "AND e.startTime < :endTime AND e.endTime > :startTime " +
                          "AND e.recurrenceRule IS NULL";
            
            if (excludeEventId != null) {
                jpql += " AND e.id != :excludeId";
            }
            
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("user", user);
            query.setParameter("startTime", startTime);
            query.setParameter("endTime", endTime);
            
            if (excludeEventId != null) {
                query.setParameter("excludeId", excludeEventId);
            }
            
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }
}
