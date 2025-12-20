package com.mycompany.model;

/**
 * Enum định nghĩa loại TodoItem.
 * Dùng để phân biệt DeadlineTask và ScheduleEvent.
 */
public enum ItemType {
    /** Deadline task - công việc có thời hạn */
    TASK,
    
    /** Schedule event - sự kiện lịch */
    EVENT
}
