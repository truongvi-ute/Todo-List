package com.mycompany.model;

import java.io.Serializable;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity đại diện cho một buổi cụ thể của ScheduleEvent.
 * Bảng: day_events
 * Đây là bảng chính để query và hiển thị lên giao diện Lịch.
 */
@Entity
@Table(name = "day_events")
public class DayEvent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ScheduleEvent cha */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_event_id", nullable = false)
    private ScheduleEvent scheduleEvent;

    /** Ngày diễn ra cụ thể */
    @Column(name = "specific_date", nullable = false)
    private LocalDate specificDate;

    /** 
     * Giờ bắt đầu ghi đè.
     * Null = sử dụng defaultStartTime từ ScheduleEvent cha.
     */
    @Column(name = "override_start_time")
    private LocalTime overrideStartTime;

    /** 
     * Giờ kết thúc ghi đè.
     * Null = sử dụng defaultEndTime từ ScheduleEvent cha.
     */
    @Column(name = "override_end_time")
    private LocalTime overrideEndTime;

    /** Trạng thái: ACTIVE hoặc CANCELLED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DayEventStatus status = DayEventStatus.ACTIVE;

    /** Constructor mặc định cho JPA */
    public DayEvent() {
    }

    /**
     * Constructor tạo DayEvent mới.
     * 
     * @param scheduleEvent ScheduleEvent cha
     * @param specificDate Ngày diễn ra
     */
    public DayEvent(ScheduleEvent scheduleEvent, LocalDate specificDate) {
        this.scheduleEvent = scheduleEvent;
        this.specificDate = specificDate;
        this.status = DayEventStatus.ACTIVE;
    }

    /**
     * Constructor đầy đủ.
     * 
     * @param scheduleEvent ScheduleEvent cha
     * @param specificDate Ngày diễn ra
     * @param overrideStartTime Giờ bắt đầu ghi đè (nullable)
     * @param overrideEndTime Giờ kết thúc ghi đè (nullable)
     * @param status Trạng thái
     */
    public DayEvent(ScheduleEvent scheduleEvent, LocalDate specificDate, 
                    LocalTime overrideStartTime, LocalTime overrideEndTime, 
                    DayEventStatus status) {
        this.scheduleEvent = scheduleEvent;
        this.specificDate = specificDate;
        this.overrideStartTime = overrideStartTime;
        this.overrideEndTime = overrideEndTime;
        this.status = status;
    }

    // --- HELPER METHODS ---

    /**
     * Lấy giờ bắt đầu thực tế.
     * Ưu tiên overrideStartTime, nếu null thì dùng defaultStartTime từ cha.
     * 
     * @return Giờ bắt đầu thực tế
     */
    public LocalTime getEffectiveStartTime() {
        return overrideStartTime != null ? overrideStartTime : scheduleEvent.getDefaultStartTime();
    }

    /**
     * Lấy giờ kết thúc thực tế.
     * Ưu tiên overrideEndTime, nếu null thì dùng defaultEndTime từ cha.
     * 
     * @return Giờ kết thúc thực tế
     */
    public LocalTime getEffectiveEndTime() {
        return overrideEndTime != null ? overrideEndTime : scheduleEvent.getDefaultEndTime();
    }

    /**
     * Kiểm tra xem buổi này có bị hủy không.
     * 
     * @return true nếu status = CANCELLED
     */
    public boolean isCancelled() {
        return status == DayEventStatus.CANCELLED;
    }

    // --- GETTERS & SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ScheduleEvent getScheduleEvent() { return scheduleEvent; }
    public void setScheduleEvent(ScheduleEvent scheduleEvent) { this.scheduleEvent = scheduleEvent; }

    public LocalDate getSpecificDate() { return specificDate; }
    public void setSpecificDate(LocalDate specificDate) { this.specificDate = specificDate; }

    public LocalTime getOverrideStartTime() { return overrideStartTime; }
    public void setOverrideStartTime(LocalTime overrideStartTime) { this.overrideStartTime = overrideStartTime; }

    public LocalTime getOverrideEndTime() { return overrideEndTime; }
    public void setOverrideEndTime(LocalTime overrideEndTime) { this.overrideEndTime = overrideEndTime; }

    public DayEventStatus getStatus() { return status; }
    public void setStatus(DayEventStatus status) { this.status = status; }
}
