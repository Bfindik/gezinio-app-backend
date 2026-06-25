package com.example.gezinio.auth.repository;

import com.example.gezinio.auth.model.RefreshToken;
import com.example.gezinio.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findByUser(User user); // kullanıcının tüm tokenlarını bul
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user = :user " +
            "AND rt.revoked = false AND rt.expiryDate > :now")
    List<RefreshToken> findActiveTokensByUser(
            @Param("user") User user,
            @Param("now") LocalDateTime now
    );
    @Modifying
    void deleteByUser(User user); // kullanıcın tokenlarını sil
    @Modifying
    void deleteByExpiryDateBefore(LocalDateTime now);// expire olmuş tokenları sil
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true " +
            "AND rt.revokedAt < :date")
    void deleteRevokedTokensOlderThan(@Param("date") LocalDateTime date); // revoked edilmiş tokenları sil
    boolean existsByUser(User user); // kullanıcın token var mı
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user " +
            "AND rt.revoked = false AND rt.expiryDate > :now")
    long countActiveTokensByUser( // kullanıcın aktif token sayısı(kaç çihazda aktif
            @Param("user") User user,
            @Param("now") LocalDateTime now
    );
}
