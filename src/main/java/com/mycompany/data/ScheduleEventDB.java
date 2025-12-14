package com.mycompany.data;

import com.mycompany.model.RecurrenceRule;
import com.mycompany.model.ScheduleEvent;
import com.mycompany.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleEventDB {

    // Thêm event mới
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

    // Tìm event theo ID
    public static ScheduleEvent findById(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(ScheduleEvent.class, id);
        } finally {
            em.close();
        }
    }

    // Cập nhật event
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

    // Xóa event
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

    // Lấy tất cả events của user trong khoảng thời gian (không lặp lại)
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

    // Lấy tất cả recurring events của user (để hiển thị trong dropdown ngoại lệ)
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

    // Lấy tất cả recurring events có thể xuất hiện trong khoảng thời gian
    public static List<ScheduleEvent> getRecurringEventsInRange(User user, LocalDate startDate, LocalDate endDate) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Lấy recurring events mà:
            // - startTime <= endDate (bắt đầu trước hoặc trong khoảng)
            // - untilDate >= startDate hoặc untilDate IS NULL (chưa kết thúc hoặc kết thúc sau khoảng)
            String jpql = "SELECT e FROM ScheduleEvent e " +
                          "LEFT JOIN FETCH e.recurrenceRule r " +
                          "WHERE e.user = :user " +
                          "AND e.recurrenceRule IS NOT NULL " +
                          "AND e.originalEvent IS NULL " +
                          "AND e.startTime <= :endDateTime " +
                          "AND (r.untilDate IS NULL OR r.untilDate >= :startDate)";
            TypedQuery<ScheduleEvent> query = em.createQuery(jpql, ScheduleEvent.class);
            query.setParameter("user", user);
            query.setParameter("startDate", startDate); // LocalDate cho untilDate
            query.setParameter("endDateTime", endDate.plusDays(1).atStartOfDay()); // LocalDateTime cho startTime
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Thêm ngày ngoại lệ vào RecurrenceRule
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

    // Tạo modified instance cho một ngày cụ thể của recurring event
    public static void createModifiedInstance(Long originalEventId, LocalDate date, 
            LocalDateTime newStartTime, LocalDateTime newEndTime) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            ScheduleEvent originalEvent = em.find(ScheduleEvent.class, originalEventId);
            if (originalEvent != null && originalEvent.getRecurrenceRule() != null) {
                // Tạo instance mới
                ScheduleEvent modifiedInstance = new ScheduleEvent(
                    originalEvent.getTitle(),
                    originalEvent.getDescription(),
                    newStartTime,
                    newEndTime,
                    originalEvent.getUser()
                );
                modifiedInstance.setOriginalEvent(originalEvent);
                em.persist(modifiedInstance);
                
                // Thêm ngày gốc vào excluded dates
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

    // Lấy modified instances của một recurring event trong khoảng thời gian
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
}
