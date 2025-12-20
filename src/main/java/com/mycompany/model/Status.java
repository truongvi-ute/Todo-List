package com.mycompany.model;

/**
 * Enum định nghĩa trạng thái của DeadlineTask.
 */
public enum Status {
    /** Task đang thực hiện */
    IN_PROGRESS,
    
    /** Task đã hoàn thành */
    DONE,
    
    /** Task quá hạn (chưa hoàn thành và đã qua deadline) */
    LATE
}
