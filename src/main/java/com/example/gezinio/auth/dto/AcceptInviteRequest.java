package com.example.gezinio.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AcceptInviteRequest {

    @NotBlank(message = "token is required")
    private String token;

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!._\\-]).*$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character (@#$%^&+=)"
    )
    private String password;

    public AcceptInviteRequest() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
