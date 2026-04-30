package com.kr.bill.inventorymangementsystem.config;

import com.kr.bill.inventorymangementsystem.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring Security configuration.
 * - Form-based login at /login
 * - Registration at /register (public)
 * - All other routes require authentication
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private final UserService userService;

    public SecurityConfig(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

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
     *   <li>Public access to registration, login, and static resources (CSS, images).</li>
     *   <li>Authenticated access required for all other requests.</li>
     *   <li>Form-based login is enabled, with custom login and logout handling.</li>
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
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/register", "/login", "/css/**", "/images/**",
                        "/manifest.json", "/service-worker.js", "/offline.html").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/stats-dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))  // accept GET or POST (CSRF disabled)
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // AJAX scan endpoint requires CSRF disabled
        return http.build();
    }
}