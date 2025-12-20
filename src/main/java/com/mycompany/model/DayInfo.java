package com.mycompany.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO (Data Transfer Object) chứa thông tin một ngày trong tuần.
 * Dùng để truyền dữ liệu từ Servlet sang JSP cho view deadline.
 */
public class DayInfo {
    
    /** Tên ngày viết tắt: Mon, Tue, Wed, ... */
    private String dayName;
    
    /** Ngày tháng format dd/MM */
    private String dateString;
    
    /** Đối tượng LocalDate */
    private LocalDate date;
    
    /** Danh sách tasks của ngày này */
    private List<DeadlineTask> tasks = new ArrayList<>();

    /** Constructor mặc định */
    public DayInfo() {
    }

    /**
     * Constructor đầy đủ.
     * 
     * @param dayName Tên ngày (Mon, Tue, ...)
     * @param dateString Ngày tháng (dd/MM)
     * @param date LocalDate object
     */
    public DayInfo(String dayName, String dateString, LocalDate date) {
        this.dayName = dayName;
        this.dateString = dateString;
        this.date = date;
    }

    // --- GETTERS ---
    
    public String getDayName() {
        return dayName;
    }

    public String getDateString() {
        return dateString;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<DeadlineTask> getTasks() {
        return tasks;
    }

    // --- SETTERS ---
    
    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setTasks(List<DeadlineTask> tasks) {
        this.tasks = tasks;
    }

    /**
     * Helper method thêm task vào ngày.
     * 
     * @param task Task cần thêm
     */
    public void addTask(DeadlineTask task) {
        this.tasks.add(task);
    }
}
