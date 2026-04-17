package com.kr.bill.inventorymangementsystem.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot {@link EnvironmentPostProcessor} that converts the
 * {@code DATABASE_URL} environment variable provided by the Render platform
 * into Spring DataSource properties before any application bean is created.
 *
 * <h3>Why this is needed</h3>
 * Render injects the database connection string as a single {@code DATABASE_URL}
 * in the format:
 * <pre>
 *   postgresql://username:password@host:port/database
 * </pre>
 * Spring Boot's auto-configuration expects separate properties:
 * <ul>
 *   <li>{@code spring.datasource.url} (JDBC URL)</li>
 *   <li>{@code spring.datasource.username}</li>
 *   <li>{@code spring.datasource.password}</li>
 *   <li>{@code spring.datasource.driverClassName}</li>
 * </ul>
 * This processor bridges that gap by parsing {@code DATABASE_URL} and
 * injecting the derived properties with the highest priority so they
 * override anything in {@code application.properties}.
 *
 * <h3>SSL Handling</h3>
 * <ul>
 *   <li><b>Internal Render hostnames</b> (e.g. {@code dpg-xxx-a}) –
 *       {@code sslmode=disable} (internal traffic, no certificate needed).</li>
 *   <li><b>External hostnames</b> (contain a dot, e.g.
 *       {@code dpg-xxx.oregon-postgres.render.com}) –
 *       {@code sslmode=require}.</li>
 * </ul>
 *
 * <h3>Registration</h3>
 * This class is registered in
 * {@code META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor}
 * so Spring Boot discovers it automatically via the SPI mechanism.
 *
 * <h3>Local Development</h3>
 * When {@code DATABASE_URL} is not set the processor exits immediately
 * and the H2 properties in {@code application.properties} remain active.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    /**
     * Parses {@code DATABASE_URL} and injects the derived JDBC datasource
     * properties into the Spring {@link ConfigurableEnvironment}.
     *
     * <p>If {@code DATABASE_URL} is absent or blank this method is a no-op,
     * leaving the local H2 configuration untouched.</p>
     *
     * @param environment the Spring environment to modify
     * @param application the running {@link SpringApplication} (unused)
     * @throws RuntimeException if {@code DATABASE_URL} is present but cannot
     *                          be parsed as a valid URI
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return; // Local dev – use H2 from application.properties
        }

        try {
            String jdbcUrl;
            String username = null;
            String password = null;

            if (databaseUrl.startsWith("jdbc:")) {
                // Already a JDBC URL – use as-is
                jdbcUrl = databaseUrl;
            } else {
                // Convert postgresql:// scheme to a proper JDBC URL
                URI uri = new URI(databaseUrl);
                String host = uri.getHost();
                int    port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath(); // e.g. /inventory_db

                // Extract credentials from the URI user-info component
                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isBlank()) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts.length > 1 ? parts[1] : "";
                }

                // Internal Render hosts have no dots; external ones require SSL
                boolean isInternal = !host.contains(".");
                String sslParam = isInternal ? "sslmode=disable" : "sslmode=require";
                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + "?" + sslParam;
            }

            // Build the property map and inject it at the highest priority
            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            props.put("spring.datasource.driverClassName", "org.postgresql.Driver");
            if (username != null) {
                props.put("spring.datasource.username", username);
                props.put("spring.datasource.password", password);
            }

            // addFirst → overrides application.properties and all other sources
            environment.getPropertySources().addFirst(
                new MapPropertySource("renderDatabaseConfig", props)
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse DATABASE_URL: " + databaseUrl, e);
        }
    }
}