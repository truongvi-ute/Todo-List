package com.mycompany.data;

import com.mycompany.model.User;
import com.mycompany.model.ScheduleEvent;
import com.mycompany.model.DeadlineTask;
import com.mycompany.model.Priority;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;

public class TestDB {
    
    public static void main(String[] args) {
        System.out.println("⏳ Đang khởi động JPA và kết nối Database...");
        
        // Bước 1: Lấy EntityManager (Lúc này Hibernate sẽ quét các Entity và tạo bảng)
        EntityManager em = JPAUtil.getEntityManager();
        
        try {
            // Bước 2: Bắt đầu giao dịch (Transaction)
            em.getTransaction().begin();

            // --- TẠO DỮ LIỆU MẪU ---

            // 1. Tạo User
            System.out.println("👤 Đang tạo User test...");
            User user = new User();
            user.setEmail("admin@test.com");
            user.setPassword("123456");
            
            // Lưu User xuống DB trước
            em.persist(user); 

            // 2. Tạo Deadline Task
            System.out.println("📝 Đang tạo Deadline Task...");
            DeadlineTask task = new DeadlineTask(
                "Kiểm tra Database", 
                "Chạy file TestDB để xem bảng có tạo không", 
                LocalDateTime.now().plusDays(3), // Hạn là 3 ngày nữa
                Priority.HIGH, 
                user
            );
            // Lưu Task
            em.persist(task);

            // 3. Tạo Schedule Event
            System.out.println("📅 Đang tạo Schedule Event...");
            ScheduleEvent event = new ScheduleEvent(
                "Họp Team", 
                "Bàn về cấu trúc Database", 
                LocalDateTime.now().plusHours(1), // Bắt đầu sau 1 tiếng
                LocalDateTime.now().plusHours(2), // Kết thúc sau 2 tiếng
                user
            );
            // Lưu Event
            em.persist(event);

            // Bước 3: Commit (Đẩy tất cả xuống DB thật)
            em.getTransaction().commit();
            
            System.out.println("--------------------------------------------------");
            System.out.println("✅ THÀNH CÔNG RỰC RỠ!");
            System.out.println("👉 Hãy mở phần mềm quản lý Database (pgAdmin, MySQL Workbench...)");
            System.out.println("👉 Kiểm tra xem đã có 4 bảng: users, deadline_tasks, schedule_events, recurrence_rules chưa.");
            System.out.println("--------------------------------------------------");

        } catch (Exception e) {
            System.out.println("❌ LỖI XẢY RA: " + e.getMessage());
            e.printStackTrace();
            
            // Nếu lỗi thì hoàn tác, không lưu rác vào DB
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            // Đóng kết nối
            em.close();
            JPAUtil.shutdown();
        }
    }
}