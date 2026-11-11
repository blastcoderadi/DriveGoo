package com.drivego.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * OOP Concept: INHERITANCE (Parent Entity)
 * User is the parent class for Customer and Admin.
 * Uses SINGLE_TABLE inheritance – all users stored in one table,
 * differentiated by the 'role' discriminator column.
 *
 * OOP Concept: ENCAPSULATION – all fields private, access through getters/setters.
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone", unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    private AuthProvider authProvider;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "active")
    private boolean active = true;

    public enum AuthProvider {
        GOOGLE, PHONE
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ===== Default and parameterized constructors =====
    protected User() {}

    protected User(String fullName, String email, String phone, AuthProvider authProvider) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.authProvider = authProvider;
    }

    // Abstract method – subclasses define their role string
    public abstract String getRole();

    // ===== Getters and Setters (Encapsulation) =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) {
        if (fullName != null && fullName.isBlank())
            throw new IllegalArgumentException("Full name cannot be blank.");
        this.fullName = fullName;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public AuthProvider getAuthProvider() { return authProvider; }
    public void setAuthProvider(AuthProvider authProvider) { this.authProvider = authProvider; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User u)) return false;
        return id != null && id.equals(u.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
