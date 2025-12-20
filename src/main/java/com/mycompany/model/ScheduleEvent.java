package com.mycompany.model;

import java.io.Serializable;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho một schedule event (sự kiện lịch).
 * Bảng: schedule_events
 * Kế thừa từ TodoItem với type = EVENT.
 * Hỗ trợ recurring events thông qua RecurrenceRule.
 */
@Entity
@Table(name = "schedule_events")
public class ScheduleEvent extends TodoItem implements Serializable { 

    /** Thời gian bắt đầu event */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /** Thời gian kết thúc event */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /** User sở hữu event này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 
     * Quy tắc lặp lại (nếu có).
     * Null = event đơn lẻ, không lặp.
     */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "recurrence_rule_id")
    private RecurrenceRule recurrenceRule;

    /** 
     * Event gốc (nếu đây là modified instance của recurring event).
     * Null = event gốc hoặc event đơn lẻ.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_event_id")
    private ScheduleEvent originalEvent;

    /** Danh sách các modified instances của recurring event này */
    @OneToMany(mappedBy = "originalEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleEvent> modifiedInstances = new ArrayList<>();

    /** Constructor mặc định cho JPA */
    public ScheduleEvent() {
        super();
        this.type = ItemType.EVENT;
    }

    /**
     * Constructor tạo event mới.
     * 
     * @param title Tiêu đề event
     * @param description Mô tả
     * @param startTime Thời gian bắt đầu
     * @param endTime Thời gian kết thúc
     * @param user User sở hữu
     */
    public ScheduleEvent(String title, String description, LocalDateTime startTime, LocalDateTime endTime, User user) {
        super(title, description, ItemType.EVENT);
        this.startTime = startTime;
        this.endTime = endTime;
        this.user = user;
    }

    // --- GETTERS & SETTERS ---

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public RecurrenceRule getRecurrenceRule() { return recurrenceRule; }
    public void setRecurrenceRule(RecurrenceRule recurrenceRule) { this.recurrenceRule = recurrenceRule; }

    public ScheduleEvent getOriginalEvent() { return originalEvent; }
    public void setOriginalEvent(ScheduleEvent originalEvent) { this.originalEvent = originalEvent; }

    public List<ScheduleEvent> getModifiedInstances() { return modifiedInstances; }
    public void setModifiedInstances(List<ScheduleEvent> modifiedInstances) { this.modifiedInstances = modifiedInstances; }
    
    /**
     * Helper method để thêm modified instance.
     * Tự động set quan hệ 2 chiều.
     * 
     * @param exceptionEvent Modified instance cần thêm
     */
    public void addModifiedInstance(ScheduleEvent exceptionEvent) {
        modifiedInstances.add(exceptionEvent);
        exceptionEvent.setOriginalEvent(this);
    }
}
