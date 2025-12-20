package com.mycompany.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity định nghĩa quy tắc lặp lại cho ScheduleEvent.
 * Bảng: recurrence_rules
 * Hỗ trợ: DAILY, WEEKLY (với byDays), MONTHLY, YEARLY.
 */
@Entity
@Table(name = "recurrence_rules")
public class RecurrenceRule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tần suất lặp: DAILY, WEEKLY, MONTHLY, YEARLY */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrequencyType frequency;

    /** Ngày kết thúc lặp (null = lặp vô hạn) */
    @Column(name = "until_date")
    private LocalDate untilDate;

    /** 
     * Danh sách các ngày trong tuần cho WEEKLY frequency.
     * VD: [MONDAY, WEDNESDAY, FRIDAY] = lặp vào T2, T4, T6.
     * Lưu trong bảng phụ recurrence_days.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recurrence_days", joinColumns = @JoinColumn(name = "rule_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> byDays = new ArrayList<>();

    /** 
     * Danh sách các ngày bị loại trừ (không diễn ra event).
     * Dùng cho exceptions: nghỉ lễ, hủy buổi học, etc.
     * Lưu trong bảng phụ recurrence_exclusions.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recurrence_exclusions", joinColumns = @JoinColumn(name = "rule_id"))
    @Column(name = "excluded_date")
    private List<LocalDate> excludedDates = new ArrayList<>();

    /** Quan hệ ngược về ScheduleEvent */
    @OneToOne(mappedBy = "recurrenceRule")
    private ScheduleEvent event;

    /** Constructor mặc định cho JPA */
    public RecurrenceRule() {
    }

    /**
     * Constructor tạo rule mới.
     * 
     * @param frequency Tần suất lặp
     * @param untilDate Ngày kết thúc (null = vô hạn)
     */
    public RecurrenceRule(FrequencyType frequency, LocalDate untilDate) {
        this.frequency = frequency;
        this.untilDate = untilDate;
    }

    // --- GETTERS & SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FrequencyType getFrequency() { return frequency; }
    public void setFrequency(FrequencyType frequency) { this.frequency = frequency; }

    public LocalDate getUntilDate() { return untilDate; }
    public void setUntilDate(LocalDate untilDate) { this.untilDate = untilDate; }

    public List<DayOfWeek> getByDays() { return byDays; }
    public void setByDays(List<DayOfWeek> byDays) { this.byDays = byDays; }

    public List<LocalDate> getExcludedDates() { return excludedDates; }
    public void setExcludedDates(List<LocalDate> excludedDates) { this.excludedDates = excludedDates; }

    public ScheduleEvent getEvent() { return event; }
    public void setEvent(ScheduleEvent event) { this.event = event; }
}
