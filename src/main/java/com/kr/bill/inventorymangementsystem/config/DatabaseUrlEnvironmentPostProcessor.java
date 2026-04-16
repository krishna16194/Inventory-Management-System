package com.kr.bill.inventorymangementsystem.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Render's DATABASE_URL (postgresql://user:pass@host:port/db)
 * into proper Spring Boot datasource properties before any bean is created.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        try {
            // Convert postgresql:// -> jdbc:postgresql://
            String jdbcUrl;
            String username = null;
            String password = null;

            if (databaseUrl.startsWith("jdbc:")) {
                jdbcUrl = databaseUrl;
            } else {
                URI uri = new URI(databaseUrl);
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath(); // e.g. /inventory_db

                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isBlank()) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts.length > 1 ? parts[1] : "";
                }

                // Internal Render hostnames (e.g. dpg-xxx-a) don't need SSL
                // External hostnames (e.g. dpg-xxx-a.oregon-postgres.render.com) require SSL
                boolean isInternal = !host.contains(".");
                String sslParam = isInternal ? "sslmode=disable" : "sslmode=require";
                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + "?" + sslParam;
            }

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            props.put("spring.datasource.driverClassName", "org.postgresql.Driver");
            if (username != null) {
                props.put("spring.datasource.username", username);
                props.put("spring.datasource.password", password);
            }

            // Add with HIGH priority so it overrides application.properties
            environment.getPropertySources().addFirst(
                new MapPropertySource("renderDatabaseConfig", props)
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse DATABASE_URL: " + databaseUrl, e);
        }
    }
}
