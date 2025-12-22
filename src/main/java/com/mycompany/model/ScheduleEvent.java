package com.mycompany.model;

import java.io.Serializable;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho một schedule event mẫu/cha.
 * Bảng: schedule_events
 * Lưu cấu hình chung của một chuỗi sự kiện.
 * Không dùng để hiển thị trực tiếp lên lịch - dùng DayEvent thay thế.
 */
@Entity
@Table(name = "schedule_events")
public class ScheduleEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tiêu đề sự kiện */
    @Column(nullable = false)
    private String title;

    /** Mô tả chi tiết */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** User sở hữu event này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Ngày bắt đầu chuỗi sự kiện */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Ngày kết thúc chuỗi sự kiện (bắt buộc) */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /** 
     * Các thứ lặp lại trong tuần.
     * Format: "MON,WED,FRI" hoặc "1,3,5"
     * Null hoặc empty = sự kiện đơn lẻ (chỉ diễn ra 1 ngày)
     */
    @Column(name = "recurrence_days")
    private String recurrenceDays;

    /** Giờ bắt đầu mặc định */
    @Column(name = "default_start_time", nullable = false)
    private LocalTime defaultStartTime;

    /** Giờ kết thúc mặc định */
    @Column(name = "default_end_time", nullable = false)
    private LocalTime defaultEndTime;

    /** Danh sách các buổi cụ thể của event này */
    @OneToMany(mappedBy = "scheduleEvent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DayEvent> dayEvents = new ArrayList<>();

    /** Constructor mặc định cho JPA */
    public ScheduleEvent() {
    }

    /**
     * Constructor tạo event mới.
     * 
     * @param title Tiêu đề event
     * @param description Mô tả
     * @param user User sở hữu
     * @param startDate Ngày bắt đầu
     * @param endDate Ngày kết thúc
     * @param recurrenceDays Các thứ lặp lại (VD: "MON,WED,FRI")
     * @param defaultStartTime Giờ bắt đầu mặc định
     * @param defaultEndTime Giờ kết thúc mặc định
     */
    public ScheduleEvent(String title, String description, User user, 
                         LocalDate startDate, LocalDate endDate, String recurrenceDays,
                         LocalTime defaultStartTime, LocalTime defaultEndTime) {
        this.title = title;
        this.description = description;
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recurrenceDays = recurrenceDays;
        this.defaultStartTime = defaultStartTime;
        this.defaultEndTime = defaultEndTime;
    }

    // --- GETTERS & SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getRecurrenceDays() { return recurrenceDays; }
    public void setRecurrenceDays(String recurrenceDays) { this.recurrenceDays = recurrenceDays; }

    public LocalTime getDefaultStartTime() { return defaultStartTime; }
    public void setDefaultStartTime(LocalTime defaultStartTime) { this.defaultStartTime = defaultStartTime; }

    public LocalTime getDefaultEndTime() { return defaultEndTime; }
    public void setDefaultEndTime(LocalTime defaultEndTime) { this.defaultEndTime = defaultEndTime; }

    public List<DayEvent> getDayEvents() { return dayEvents; }
    public void setDayEvents(List<DayEvent> dayEvents) { this.dayEvents = dayEvents; }

    /**
     * Helper method để thêm DayEvent.
     * Tự động set quan hệ 2 chiều.
     * 
     * @param dayEvent DayEvent cần thêm
     */
    public void addDayEvent(DayEvent dayEvent) {
        dayEvents.add(dayEvent);
        dayEvent.setScheduleEvent(this);
    }

    /**
     * Helper method để xóa DayEvent.
     * 
     * @param dayEvent DayEvent cần xóa
     */
    public void removeDayEvent(DayEvent dayEvent) {
        dayEvents.remove(dayEvent);
        dayEvent.setScheduleEvent(null);
    }
}
