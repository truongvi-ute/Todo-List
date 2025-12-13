package com.mycompany.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO để truyền dữ liệu ngày và danh sách task cho view
 */
public class DayInfo {
    
    private String dayName;
    private String dateString;
    private LocalDate date;
    private List<DeadlineTask> tasks = new ArrayList<>();

    public DayInfo() {
    }

    public DayInfo(String dayName, String dateString, LocalDate date) {
        this.dayName = dayName;
        this.dateString = dateString;
        this.date = date;
    }

    // Getters
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

    // Setters
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

    // Helper method
    public void addTask(DeadlineTask task) {
        this.tasks.add(task);
    }
}
