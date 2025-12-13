package com.mycompany.model;

import jakarta.persistence.*; // Dùng javax cho Tomcat 9
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class TodoItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false)
    protected String title;

    @Column(columnDefinition = "TEXT")
    protected String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    protected ItemType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    protected LocalDateTime createdAt;

    // --- 1. CONSTRUCTOR RỖNG (BẮT BUỘC CHO JPA) ---
    // JPA cần cái này để khởi tạo object qua Reflection
    protected TodoItem() {
    }

    // --- 2. CONSTRUCTOR ĐẦY ĐỦ (CHO LẬP TRÌNH VIÊN DÙNG) ---
    // Lưu ý: Không truyền ID (vì tự tăng), Không truyền CreatedAt (vì tự gán)
    public TodoItem(String title, String description, ItemType type) {
        this.title = title;
        this.description = description;
        this.type = type;
    }

    // --- Lifecycle Callback ---
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ItemType getType() { return type; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}