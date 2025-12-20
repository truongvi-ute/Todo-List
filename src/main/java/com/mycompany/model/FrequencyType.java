package com.mycompany.model;

/**
 * Enum định nghĩa tần suất lặp lại của RecurrenceRule.
 */
public enum FrequencyType {
    /** Lặp hàng ngày */
    DAILY,
    
    /** Lặp hàng tuần (có thể chỉ định byDays) */
    WEEKLY,
    
    /** Lặp hàng tháng (cùng ngày trong tháng) */
    MONTHLY,
    
    /** Lặp hàng năm (cùng ngày tháng) */
    YEARLY
}
