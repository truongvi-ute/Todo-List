package com.mycompany.data;

import com.mycompany.model.DayEvent;
import com.mycompany.model.DayEventStatus;
import com.mycompany.model.ScheduleEvent;
import com.mycompany.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Access Object (DAO) cho entity ScheduleEvent.
 * Cung cấp các phương thức CRUD và truy vấn cho schedule events.
 */
public class ScheduleEventDB {

    /**
     * Thêm event mới vào database và tự động generate các DayEvents.
     * 
     * @param event ScheduleEvent object cần lưu
     */
    public static void insert(ScheduleEvent event) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(event);
            
            // Generate DayEvents dựa trên recurrenceDays
            generateDayEvents(event, em);
            
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
     * Nếu thay đổi recurrenceDays hoặc ngày, sẽ regenerate DayEvents.
     * 
     * @param event ScheduleEvent object đã được chỉnh sửa
     * @param regenerateDayEvents true nếu cần tạo lại DayEvents
     */
    public static void update(ScheduleEvent event, boolean regenerateDayEvents) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            
            if (regenerateDayEvents) {
                // Xóa tất cả DayEvents cũ
                String deleteJpql = "DELETE FROM DayEvent d WHERE d.scheduleEvent.id = :eventId";
                em.createQuery(deleteJpql).setParameter("eventId", event.getId()).executeUpdate();
                
                // Merge event trước
                event = em.merge(event);
                
                // Generate lại DayEvents
                generateDayEvents(event, em);
            } else {
                em.merge(event);
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
     * Cập nhật thông tin event (không regenerate DayEvents).
     * 
     * @param event ScheduleEvent object đã được chỉnh sửa
     */
    public static void update(ScheduleEvent event) {
        update(event, false);
    }

    /**
     * Xóa event khỏi database.
     * Xóa DayEvents trước, sau đó xóa ScheduleEvent.
     * 
     * @param id ID của event cần xóa
     */
    public static void delete(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            
            // Xóa tất cả DayEvents trước
            String deleteChildrenJpql = "DELETE FROM DayEvent d WHERE d.scheduleEvent.id = :eventId";
            em.createQuery(deleteChildrenJpql).setParameter("eventId", id).executeUpdate();
            
            // Sau đó xóa ScheduleEvent
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
     * Lấy tất cả ScheduleEvents của user.
     * Dùng để hiển thị trong dropdown quản lý.
     * 
     * @param user User sở hữu events
     * @return Danh sách ScheduleEvents
     */
    public static List<ScheduleEvent> getAllByUser(User user) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT e FROM ScheduleEvent e WHERE e.user = :user ORDER BY e.title ASC";
            TypedQuery<ScheduleEvent> query = em.createQuery(jpql, ScheduleEvent.class);
            query.setParameter("user", user);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lấy ScheduleEvents có recurring (có recurrenceDays).
     * 
     * @param user User sở hữu events
     * @return Danh sách recurring ScheduleEvents
     */
    public static List<ScheduleEvent> getRecurringEventsByUser(User user) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT e FROM ScheduleEvent e WHERE e.user = :user " +
                          "AND e.recurrenceDays IS NOT NULL AND e.recurrenceDays != '' " +
                          "ORDER BY e.title ASC";
            TypedQuery<ScheduleEvent> query = em.createQuery(jpql, ScheduleEvent.class);
            query.setParameter("user", user);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Generate các DayEvent dựa trên cấu hình của ScheduleEvent.
     * 
     * @param event ScheduleEvent cha
     * @param em EntityManager đang active
     */
    private static void generateDayEvents(ScheduleEvent event, EntityManager em) {
        LocalDate startDate = event.getStartDate();
        LocalDate endDate = event.getEndDate();
        String recurrenceDays = event.getRecurrenceDays();
        
        // Parse recurrenceDays thành list DayOfWeek
        List<DayOfWeek> targetDays = parseRecurrenceDays(recurrenceDays);
        
        // Nếu không có recurrenceDays, tạo 1 DayEvent duy nhất cho startDate
        if (targetDays.isEmpty()) {
            DayEvent dayEvent = new DayEvent(event, startDate);
            em.persist(dayEvent);
            return;
        }
        
        // Duyệt từ startDate đến endDate, tạo DayEvent cho các ngày khớp
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (targetDays.contains(current.getDayOfWeek())) {
                DayEvent dayEvent = new DayEvent(event, current);
                em.persist(dayEvent);
            }
            current = current.plusDays(1);
        }
    }

    /**
     * Parse chuỗi recurrenceDays thành list DayOfWeek.
     * Hỗ trợ format: "MON,WED,FRI" hoặc "1,3,5" (1=Monday)
     * 
     * @param recurrenceDays Chuỗi các ngày
     * @return List DayOfWeek
     */
    private static List<DayOfWeek> parseRecurrenceDays(String recurrenceDays) {
        if (recurrenceDays == null || recurrenceDays.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<DayOfWeek> result = new ArrayList<>();
        
        for (String day : recurrenceDays.split(",")) {
            day = day.trim().toUpperCase();
            if (day.isEmpty()) continue;
            
            try {
                // Thử parse số trước (1-7, 1=Monday)
                int dayNum = Integer.parseInt(day);
                result.add(DayOfWeek.of(dayNum));
            } catch (NumberFormatException e) {
                // Parse tên ngày viết tắt
                switch (day) {
                    case "MON": result.add(DayOfWeek.MONDAY); break;
                    case "TUE": result.add(DayOfWeek.TUESDAY); break;
                    case "WED": result.add(DayOfWeek.WEDNESDAY); break;
                    case "THU": result.add(DayOfWeek.THURSDAY); break;
                    case "FRI": result.add(DayOfWeek.FRIDAY); break;
                    case "SAT": result.add(DayOfWeek.SATURDAY); break;
                    case "SUN": result.add(DayOfWeek.SUNDAY); break;
                    default:
                        // Thử parse tên đầy đủ
                        try {
                            result.add(DayOfWeek.valueOf(day));
                        } catch (IllegalArgumentException ex) {
                            System.out.println("Invalid day: " + day);
                        }
                }
            }
        }
        
        return result;
    }

    /**
     * Kiểm tra xem có DayEvent nào trùng giờ không trong ngày cụ thể.
     * 
     * @param user User sở hữu events
     * @param date Ngày cần kiểm tra
     * @param startTime Giờ bắt đầu
     * @param endTime Giờ kết thúc
     * @param excludeDayEventId ID của DayEvent cần loại trừ (khi edit), null nếu thêm mới
     * @return true nếu có trùng giờ
     */
    public static boolean hasOverlappingEvent(User user, LocalDate date, 
            LocalTime startTime, LocalTime endTime, Long excludeDayEventId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Lấy tất cả DayEvents ACTIVE trong ngày đó của user
            String jpql = "SELECT d FROM DayEvent d " +
                          "WHERE d.scheduleEvent.user = :user " +
                          "AND d.specificDate = :date " +
                          "AND d.status = :status";
            
            if (excludeDayEventId != null) {
                jpql += " AND d.id != :excludeId";
            }
            
            TypedQuery<DayEvent> query = em.createQuery(jpql, DayEvent.class);
            query.setParameter("user", user);
            query.setParameter("date", date);
            query.setParameter("status", DayEventStatus.ACTIVE);
            
            if (excludeDayEventId != null) {
                query.setParameter("excludeId", excludeDayEventId);
            }
            
            List<DayEvent> existingEvents = query.getResultList();
            
            // Kiểm tra overlap với từng event
            for (DayEvent existing : existingEvents) {
                LocalTime existingStart = existing.getEffectiveStartTime();
                LocalTime existingEnd = existing.getEffectiveEndTime();
                
                // Overlap: newStart < existingEnd AND newEnd > existingStart
                if (startTime.isBefore(existingEnd) && endTime.isAfter(existingStart)) {
                    return true;
                }
            }
            
            return false;
        } finally {
            em.close();
        }
    }
}
