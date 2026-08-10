package com.qametrics.portal.domain.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio pura — CERO dependencias de Spring o JPA.
 * Representa un usuario del sistema QA Metrics Portal.
 */
public class User {

    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;

    public User() {}

    public User(Long id, String username, String email,
                String passwordHash, UserRole role,
                boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    // ── Lógica de negocio pura ──────────────────────────────
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role) || UserRole.ROLE_ADMIN.equals(this.role);
    }

    public boolean isAnalyst() {
        return UserRole.ANALYST.equals(this.role) || UserRole.ROLE_ANALYST.equals(this.role);
    }

    // ── Getters & Setters ───────────────────────────────────
    public Long getId()                { return id; }
    public void setId(Long id)         { this.id = id; }

    public String getUsername()              { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail()           { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash()                  { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public UserRole getRole()          { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive()           { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt()              { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
}
