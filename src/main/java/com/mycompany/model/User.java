package com.mycompany.model;

import jakarta.persistence.*; // Nếu dùng Hibernate cũ (bản < 6) thì đổi thành jakarta.persistence.*
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users") // Đặt tên bảng là 'users' để tránh trùng từ khóa 'User' trong SQL (ví dụ Postgres)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;


    // --- QUAN HỆ VỚI DEADLINE TASK ---
    // mappedBy = "user": Nghĩa là biến 'user' bên class DeadlineTask nắm giữ khóa ngoại
    // CascadeType.ALL: Xóa User thì xóa luôn Task của họ
    // orphanRemoval = true: Nếu xóa Task khỏi list này, nó sẽ bị xóa khỏi DB
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DeadlineTask> tasks = new ArrayList<>();

    // --- QUAN HỆ VỚI SCHEDULE EVENT ---
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ScheduleEvent> events = new ArrayList<>();

    // --- CONSTRUCTORS ---
    public User() {
    }

    public User(String email, String password) {
        this.password = password;
        this.email = email;
    }

    // --- HELPER METHODS CHO QUAN HỆ 2 CHIỀU (Best Practice) ---
    // Giúp đồng bộ dữ liệu giữa List của User và biến user bên Task
//    
//    public void addDeadlineTask(DeadlineTask task) {
//        tasks.add(task);
//        task.setUser(this);
//    }
//
//    public void removeDeadlineTask(DeadlineTask task) {
//        tasks.remove(task);
//        task.setUser(null);
//    }
//
//    public void addScheduleEvent(ScheduleEvent event) {
//        events.add(event);
//        event.setUser(this);
//    }
//
//    public void removeScheduleEvent(ScheduleEvent event) {
//        events.remove(event);
//        event.setUser(null);
//    }

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
}