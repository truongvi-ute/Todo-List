package com.mycompany.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho người dùng trong hệ thống.
 * Bảng: users
 * Quan hệ: 1-N với DeadlineTask, 1-N với ScheduleEvent
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mật khẩu đã được hash bằng BCrypt */
    @Column(nullable = false)
    private String password;

    /** Email dùng để đăng nhập, phải unique */
    @Column(nullable = false, unique = true)
    private String email;
    
    /** Bật/tắt nhận email nhắc nhở hàng ngày */
    @Column(name = "notification_enabled")
    private Boolean notificationEnabled = false;
    
    /** Giờ gửi email nhắc nhở (0-23), mặc định 6h sáng */
    @Column(name = "notification_hour")
    private Integer notificationHour = 6;
    
    /** Trạng thái bị chặn - Admin có thể chặn user không cho đăng nhập */
    @Column(name = "is_blocked")
    private Boolean isBlocked = false;

    /**
     * Danh sách deadline tasks của user.
     * CascadeType.ALL: Xóa user sẽ xóa luôn tasks.
     * orphanRemoval: Xóa task khỏi list sẽ xóa khỏi DB.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DeadlineTask> tasks = new ArrayList<>();

    /** Danh sách schedule events của user */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ScheduleEvent> events = new ArrayList<>();

    /** Constructor mặc định cho JPA */
    public User() {
    }

    /**
     * Constructor tạo user mới.
     * 
     * @param email Email đăng nhập
     * @param password Mật khẩu đã hash
     */
    public User(String email, String password) {
        this.password = password;
        this.email = email;
    }

    // --- GETTERS & SETTERS ---
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<DeadlineTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<DeadlineTask> tasks) {
        this.tasks = tasks;
    }

    public List<ScheduleEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ScheduleEvent> events) {
        this.events = events;
    }
    
    public Boolean getNotificationEnabled() {
        return notificationEnabled != null ? notificationEnabled : false;
    }
    
    public void setNotificationEnabled(Boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
    
    public Integer getNotificationHour() {
        return notificationHour != null ? notificationHour : 6;
    }
    
    public void setNotificationHour(Integer notificationHour) {
        this.notificationHour = notificationHour;
    }
    
    public Boolean getIsBlocked() {
        return isBlocked != null ? isBlocked : false;
    }
    
    public void setIsBlocked(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
}
