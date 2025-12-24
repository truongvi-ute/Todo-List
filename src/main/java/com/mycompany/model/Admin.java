package com.mycompany.model;

import jakarta.persistence.*;

/**
 * Entity đại diện cho Admin trong hệ thống.
 * Bảng: admins
 * Admin có quyền quản lý users (chặn/bỏ chặn).
 */
@Entity
@Table(name = "admins")
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Email dùng để đăng nhập, phải unique */
    @Column(nullable = false, unique = true)
    private String email;

    /** Mật khẩu đã được hash bằng BCrypt */
    @Column(nullable = false)
    private String password;

    /** Constructor mặc định cho JPA */
    public Admin() {
    }

    /**
     * Constructor tạo admin mới.
     * 
     * @param email Email đăng nhập
     * @param password Mật khẩu đã hash
     */
    public Admin(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // --- GETTERS & SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
