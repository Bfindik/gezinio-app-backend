package com.example.gezinio.auth.service;

import com.example.gezinio.auth.model.RefreshToken;
import com.example.gezinio.auth.model.User;
import com.example.gezinio.auth.repository.RefreshTokenRepository;
import com.example.gezinio.auth.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
// RefreshToken entity'lerini yönetmek
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtService jwtService;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {

        // mevcut tokenı sil
        refreshTokenRepository.deleteByUser(user);

        // yeni token oluştur
        String tokenString = jwtService.generateRefreshToken(
                user.getUsername(),
                user.getId()
        );

        // refresh token entity oluşturma
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(tokenString); // ← JWT string
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7)); // 7 gün
        refreshToken.setRevoked(false);

        // database kaydet
        return refreshTokenRepository.save(refreshToken);

    }

    @Transactional(readOnly = true)
    public RefreshToken verifyRefreshToken(String tokenString) {

        // db'den bul
        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new RuntimeException(
                        "Refresh token not found or invalid"
                ));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException(
                    "Refresh token has been revoked"
            );
        }

        if (refreshToken.isExpired()) {
            // Expire olan token'ı sil (cleanup)
            refreshTokenRepository.delete(refreshToken);

            throw new RuntimeException(
                    "Refresh token has expired. Please login again."
            );
        }

        // token geçerli
        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(RefreshToken refreshToken) {
        // soft delete
        refreshToken.revoke(); // revoked = true, revokedAt = now
        refreshTokenRepository.save(refreshToken);
    }

    // logout, user silme veya şüpheli aktivitede
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    // expire olmuş tokenlar için scheduled task
    @Transactional
    public void deleteExpiredTokens() {
        // db'de yer tutmaması için
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
    }

    // revoke edilen tokenları silme periyodik işlem
    @Transactional
    public void deleteOldRevokedTokens(LocalDateTime before) {
        // bir süre audit trail için tutup sonra sil
        refreshTokenRepository.deleteRevokedTokensOlderThan(before);
    }

    @Transactional(readOnly = true)
    public List<RefreshToken> getUserActiveTokens(User user) {
        // kullanıcının aktif tokenlarını bul

        return refreshTokenRepository.findActiveTokensByUser(
                user,
                LocalDateTime.now()
        );
    }
}