package com.mycompany.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recurrence_rules")
public class RecurrenceRule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FrequencyType frequency; // Enum: DAILY, WEEKLY, MONTHLY

    @Column(name = "until_date")
    private LocalDate untilDate; // Lặp đến ngày nào thì dừng

    // Lưu danh sách các ngày trong tuần (VD: MONDAY, WEDNESDAY)
    // @ElementCollection giúp lưu List đơn giản vào bảng phụ mà không cần tạo Entity mới
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recurrence_days", joinColumns = @JoinColumn(name = "rule_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> byDays = new ArrayList<>();

    // Lưu danh sách các ngày bị hủy (Nghỉ, không diễn ra sự kiện)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "recurrence_exclusions", joinColumns = @JoinColumn(name = "rule_id"))
    @Column(name = "excluded_date")
    private List<LocalDate> excludedDates = new ArrayList<>();

    // Quan hệ ngược về ScheduleEvent (nếu cần truy vấn ngược)
    @OneToOne(mappedBy = "recurrenceRule")
    private ScheduleEvent event;

    public RecurrenceRule() {
    }

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