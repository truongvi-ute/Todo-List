package com.mycompany.model;

import java.io.Serializable;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho một deadline task.
 * Bảng: deadline_tasks
 * Kế thừa từ TodoItem với type = TASK.
 */
@Entity
@Table(name = "deadline_tasks")
public class DeadlineTask extends TodoItem implements Serializable {

    /** Thời hạn hoàn thành task */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    /** Độ ưu tiên: LOW, MEDIUM, HIGH */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Priority priority;

    /** Trạng thái: IN_PROGRESS, DONE, LATE */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status;

    /** User sở hữu task này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Constructor mặc định cho JPA */
    public DeadlineTask() {
        super();
        this.type = ItemType.TASK; 
    }

    /**
     * Constructor tạo task mới.
     * Status mặc định là IN_PROGRESS.
     * 
     * @param title Tiêu đề task
     * @param description Mô tả
     * @param dueDate Thời hạn
     * @param priority Độ ưu tiên
     * @param user User sở hữu
     */
    public DeadlineTask(String title, String description, LocalDateTime dueDate, Priority priority, User user) {
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
