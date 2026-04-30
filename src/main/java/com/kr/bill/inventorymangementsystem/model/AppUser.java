package com.kr.bill.inventorymangementsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * JPA entity representing an application user.
 *
 * <p>Each user has their own isolated set of products and bills.
 * Passwords are stored BCrypt-encoded.</p>
 */
@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique login username. */
    @Column(unique = true, nullable = false)
    private String username;

    /** BCrypt-encoded password. */
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    /** Spring Security role, e.g. ROLE_USER or ROLE_ADMIN. */
    @Column(nullable = false)
    private String role = "ROLE_USER";

    /** Display name shown in the UI (defaults to username if blank). */
    private String displayName;
}
