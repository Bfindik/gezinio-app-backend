package com.example.agentapp.auth.controller;

import com.example.agentapp.auth.dto.*;
import com.example.agentapp.auth.security.UserPrincipal;
import com.example.agentapp.auth.service.AuthService;
import com.example.agentapp.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600) // CORS - Production'da değiştir!
@Tag(name = "Auth", description = "User registration, login, logout, and token refresh endpoints")
public class AuthController {


    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse authResponse = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", authResponse));
    }


    @PostMapping("/login")
    @Operation(summary = "Authenticate user and issue JWT access and refresh tokens")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authResponse)
        );
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out the current user and invalidate refresh tokens")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        authService.logout(userId);

        return ResponseEntity.ok(
                ApiResponse.success("Logout successful")
        );
    }

    @PostMapping("/refresh")
    @Operation(summary = "Issue a new access token using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse authResponse = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", authResponse)
        );
    }

    @GetMapping("/invite/{token}")
    @Operation(summary = "Preview a staff invite token (no consumption) — used by the activation page to greet the employee")
    public ResponseEntity<ApiResponse<InvitePreviewDTO>> previewInvite(@PathVariable String token) {
        InvitePreviewDTO preview = authService.peekInvite(token);
        return ResponseEntity.ok(ApiResponse.success("Invite is valid", preview));
    }

    @PostMapping("/activate")
    @Operation(summary = "Accept a staff invite: set a password and receive a fresh JWT session")
    public ResponseEntity<ApiResponse<AuthResponse>> activate(
            @Valid @RequestBody AcceptInviteRequest request) {
        AuthResponse authResponse = authService.activateAccount(request);
        return ResponseEntity.ok(ApiResponse.success("Account activated", authResponse));
    }

}