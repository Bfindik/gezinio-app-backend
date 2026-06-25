package com.example.gezinio.auth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Permission Entity
 *
 * Yetkileri database'de saklar.
 * Role entity ile Many-to-Many ilişkisi vardır.
 *
 * GÜVENLİK:
 * - Enum-based: Type-safe
 * - Unique: Aynı permission iki kez olamaz
 * - Immutable: Permission'lar değişmez
 */
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false, length = 100)
    private PermissionName name;

    @Column(length = 200)
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ==================== CONSTRUCTORS ====================

    public Permission() {}

    public Permission(PermissionName name) {
        this.name = name;
    }

    public Permission(PermissionName name, String description) {
        this.name = name;
        this.description = description;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PermissionName getName() {
        return name;
    }

    public void setName(PermissionName name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission)) return false;
        Permission that = (Permission) o;
        return name == that.name;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
