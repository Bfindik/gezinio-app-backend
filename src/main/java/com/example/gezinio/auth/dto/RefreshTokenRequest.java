package com.example.gezinio.auth.dto;

import jakarta.validation.constraints.NotBlank;


public class RefreshTokenRequest {
    // Token yenileme endpoint'i için.
    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;

    public RefreshTokenRequest() {}

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}