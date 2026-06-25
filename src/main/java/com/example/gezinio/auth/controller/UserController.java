package com.example.gezinio.auth.controller;

import com.example.gezinio.auth.dto.ChangePasswordRequest;
import com.example.gezinio.auth.dto.UserDTO;
import com.example.gezinio.auth.security.UserPrincipal;
import com.example.gezinio.auth.service.UserService;
import com.example.gezinio.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Users", description = "User profile self-service and administrative user lookup endpoints")
public class UserController {


    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user's profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {

        UserDTO user = userService.getCurrentUser();

        return ResponseEntity.ok(
                ApiResponse.success("User retrieved successfully", user)
        );
    }

    @PutMapping("/me")
    @Operation(summary = "Update the current user's profile fields")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            Authentication authentication,
            @RequestBody Map<String, String> updates) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        UserDTO updatedUser = userService.updateProfile(
                userId,
                updates.get("firstName"),
                updates.get("lastName"),
                updates.get("phone")
        );

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", updatedUser)
        );
    }

    @PostMapping("/me/change-password")
    @Operation(summary = "Change the current user's password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        userService.changePassword(userId, request);

        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully")
        );
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete the current user's own account after password confirmation")
    public ResponseEntity<ApiResponse<Void>> deleteOwnAccount(
            @RequestBody Map<String, String> request) {

        String password = request.get("password");

        userService.deleteOwnAccount(password);

        return ResponseEntity.ok(
                ApiResponse.success("Account deleted successfully")
        );
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    @Operation(summary = "List all users (SUPER_ADMIN/MANAGER only)")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {

        List<UserDTO> users = userService.getAllUsers();

        return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users)
        );
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    @Operation(summary = "Get a user by ID (SUPER_ADMIN/MANAGER only)")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {

        UserDTO user = userService.getUserById(id);

        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Soft-delete a user by ID (SUPER_ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {

        userService.softDeleteUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully")
        );
    }
}