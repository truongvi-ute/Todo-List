package com.mycompany.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Utility class quản lý kết nối JPA/Hibernate.
 * Sử dụng Singleton pattern cho EntityManagerFactory.
 */
public class JPAUtil {

    // Tên persistence unit phải khớp với file persistence.xml
    private static final String PERSISTENCE_UNIT_NAME = "todolistPU";

    private static EntityManagerFactory factory;

    /**
     * Lấy EntityManagerFactory singleton.
     * Khởi tạo factory nếu chưa tồn tại.
     * 
     * @return EntityManagerFactory instance
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        if (factory == null) {
            try {
                factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            } catch (Exception e) {
                System.out.println("❌ LỖI KHỞI TẠO JPA: " + e.getMessage());
            }
        }
        return factory;
    }

    /**
     * Tạo EntityManager mới để thao tác với database.
     * Mỗi request nên dùng một EntityManager riêng và đóng sau khi dùng xong.
     * 
     * @return EntityManager instance mới
     */
    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Đóng EntityManagerFactory khi shutdown ứng dụng.
     * Giải phóng tài nguyên kết nối database.
     */
    public static void shutdown() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }
}
