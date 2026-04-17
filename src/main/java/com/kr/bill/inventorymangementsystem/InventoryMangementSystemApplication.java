package com.kr.bill.inventorymangementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Inventory Management System Spring Boot application.
 *
 * <p>This application provides a web-based interface for managing product
 * inventory, processing sales via barcode scanning, and viewing business
 * statistics. It runs on an embedded Tomcat server on port {@code 8080}.</p>
 *
 * <h2>Profiles</h2>
 * <ul>
 *   <li><b>default</b> – H2 embedded file database ({@code inventory-db.mv.db})</li>
 *   <li><b>render</b>  – PostgreSQL database configured via the {@code DATABASE_URL}
 *       environment variable (set automatically by the Render platform)</li>
 * </ul>
 *
 * <h2>Quick Start</h2>
 * <pre>
 *   ./gradlew bootRun          # starts on http://localhost:8080
 * </pre>
 *
 * @see com.kr.bill.inventorymangementsystem.config.SecurityConfig
 * @see com.kr.bill.inventorymangementsystem.config.DatabaseUrlEnvironmentPostProcessor
 */
@SpringBootApplication
public class InventoryMangementSystemApplication {

    /**
     * Application entry point. Bootstraps the Spring context and starts the
     * embedded Tomcat server.
     *
     * @param args command-line arguments (passed through to Spring Boot)
     */
    public static void main(String[] args) {
        SpringApplication.run(InventoryMangementSystemApplication.class, args);
    }
}