package com.mycompany.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {

    // LƯU Ý QUAN TRỌNG:
    // Chuỗi "TodoPU" này phải GIỐNG HỆT tên trong file persistence.xml:
    // <persistence-unit name="TodoPU" ...>
    private static final String PERSISTENCE_UNIT_NAME = "todolistPU";

    private static EntityManagerFactory factory;

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

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void shutdown() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }
}