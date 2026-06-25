package com.example.gezinio.auth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * RefreshToken Entity
 * JWT refresh token'larını database'de saklar.
 * Access Token (24 saat):
 * - Stateless, database'de saklanmaz
 * - Her API isteğinde gönderilir
 * - Çalınırsa zararı sınırlı
 * Refresh Token (7 gün):
 * - Database'de saklanır
 * - İPTAL EDİLEBİLİR (revoke)
 * - Çalınırsa manuel iptal edilir
 * - Sadece token yenilemek için kullanılır
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_user_id", columnList = "user_id")
})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * TOKEN SAHİBİ
     * ManyToOne: Bir user birden fazla refresh token'a sahip olabilir
     * (Farklı cihazlar için)
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * TOKEN STRING
     * GÜVENLİK:
     * - Unique: Her token benzersiz
     * - Rastgele üretilir (UUID)
     * - Tahmin edilemez
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     * SON KULLANMA TARİHİ
     * Genelde 7 gün
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * İPTAL EDİLDİ Mİ?
     * GÜVENLİK: Soft delete
     * - Token silinmez, sadece revoke edilir
     * - Audit trail korunur
     */
    @Column(name = "revoked")
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ==================== CONSTRUCTORS ====================

    public RefreshToken() {}

    public RefreshToken(User user, String token, LocalDateTime expiryDate) {
        this.user = user;
        this.token = token;
        this.expiryDate = expiryDate;
    }

    // ==================== HELPER METHODS ====================

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void revoke() { // token ı iptal et logout veya şüpheli aktivitede
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    public boolean isValid() { // token kullanılabilir mi
        return !isExpired() && !isRevoked();
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}
