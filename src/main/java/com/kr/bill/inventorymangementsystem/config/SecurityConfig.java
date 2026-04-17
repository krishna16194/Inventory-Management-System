package com.kr.bill.inventorymangementsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Security configuration for the Inventory Management System.
 *
 * <p><strong>Current security posture: authentication is disabled.</strong>
 * All HTTP requests are permitted without credentials and CSRF protection is
 * turned off. This is intentional for the current single-operator, internal
 * deployment model.</p>
 *
 * <p>If authentication needs to be added in the future:</p>
 * <ol>
 *   <li>Restore a {@code UserDetailsService} bean (e.g. in-memory or JDBC).</li>
 *   <li>Add a {@code PasswordEncoder} bean (e.g. {@code BCryptPasswordEncoder}).</li>
 *   <li>Update {@link #securityFilterChain} to require authentication on
 *       protected paths and re-enable CSRF.</li>
 * </ol>
 *
 * <h3>URL Routing</h3>
 * The root path ({@code /}) is redirected to {@code /stats-dashboard}, which
 * serves as the application's landing page.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    /**
     * Registers a simple view-controller that redirects the root URL ({@code /})
     * to the Statistics Dashboard ({@code /stats-dashboard}).
     *
     * @param registry the Spring MVC view-controller registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/stats-dashboard");
    }

    /**
     * Defines the security filter chain applied to all HTTP requests.
     *
     * <p>Current rules:</p>
     * <ul>
     *   <li>All requests are permitted (no authentication required).</li>
     *   <li>CSRF protection is disabled to simplify AJAX form submissions
     *       (e.g. barcode-scan endpoint {@code POST /billing/add-scan}).</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} builder provided by Spring Security
     * @return the configured {@link SecurityFilterChain}
     * @throws Exception if the security configuration cannot be applied
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable()); // AJAX scan endpoint requires CSRF disabled
        return http.build();
    }
}