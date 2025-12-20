package com.mycompany.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Abstract base class cho các loại todo item.
 * Sử dụng @MappedSuperclass để các thuộc tính được kế thừa xuống subclass.
 * Subclasses: DeadlineTask, ScheduleEvent
 */
@MappedSuperclass
public abstract class TodoItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    /** Tiêu đề của item */
    @Column(nullable = false)
    protected String title;

    /** Mô tả chi tiết (có thể null) */
    @Column(columnDefinition = "TEXT")
    protected String description;

    /** Loại item: TASK hoặc EVENT */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    protected ItemType type;

    /** Thời gian tạo, tự động gán khi persist */
    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    /** Constructor mặc định cho JPA */
    protected TodoItem() {
    }

    /**
     * Constructor đầy đủ.
     * 
     * @param title Tiêu đề
     * @param description Mô tả
     * @param type Loại item (TASK/EVENT)
     */
    public TodoItem(String title, String description, ItemType type) {
        this.title = title;
        this.description = description;
        this.type = type;
    }

    /**
     * JPA Lifecycle callback - tự động gán createdAt trước khi persist.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- GETTERS & SETTERS ---
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ItemType getType() { return type; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}
