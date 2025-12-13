package com.mycompany.model;

import java.io.Serializable;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deadline_tasks")
public class DeadlineTask extends TodoItem implements Serializable {

    // --- CÁC THUỘC TÍNH RIÊNG CỦA DEADLINE TASK ---

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Priority priority; // Enum: LOW, MEDIUM, HIGH

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status;     // Enum: TODO, DOING, DONE

    // Quan hệ Many-to-One với User (Khóa ngoại user_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public DeadlineTask() {
        super();
        this.type = ItemType.TASK; 
    }

    public DeadlineTask(String title, String description, LocalDateTime dueDate, Priority priority, User user) {
        // Gọi constructor cha để set Title, Description và gán cứng Type là TASK
        super(title, description, ItemType.TASK);
        
        this.dueDate = dueDate;
        this.priority = priority;
        this.user = user;
        
        this.status = Status.IN_PROGRESS;
    }

    // --- GETTERS & SETTERS ---

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}