package com.mycompany.model;

import java.io.Serializable;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedule_events")
public class ScheduleEvent extends TodoItem implements Serializable{ 

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "recurrence_rule_id")
    private RecurrenceRule recurrenceRule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_event_id")
    private ScheduleEvent originalEvent;

    @OneToMany(mappedBy = "originalEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleEvent> modifiedInstances = new ArrayList<>();

    public ScheduleEvent() {
        super();
        this.type = ItemType.EVENT;
    }

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
    
    // Helper method để thêm modified instance
    public void addModifiedInstance(ScheduleEvent exceptionEvent) {
        modifiedInstances.add(exceptionEvent);
        exceptionEvent.setOriginalEvent(this);
    }
}