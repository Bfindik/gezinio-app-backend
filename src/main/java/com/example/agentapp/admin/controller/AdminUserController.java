package com.example.agentapp.admin.controller;

import com.example.agentapp.admin.dto.AdminUserDTO;
import com.example.agentapp.admin.dto.ChangeRoleRequest;
import com.example.agentapp.admin.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Users", description = "Administrative user account management: status, lockout, and role assignments")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Autowired
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "List all users with admin-level detail")
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by ID with admin-level detail")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Enable or disable a user account")
    public ResponseEntity<AdminUserDTO> setUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(adminUserService.setUserStatus(id, enabled));
    }

    @PatchMapping("/{id}/unlock")
    @Operation(summary = "Unlock a user account locked by failed login attempts")
    public ResponseEntity<AdminUserDTO> unlockUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.unlockUser(id));
    }

    @PatchMapping("/{id}/roles")
    @Operation(summary = "Replace the role assignments of a user")
    public ResponseEntity<AdminUserDTO> changeRoles(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest request) {
        return ResponseEntity.ok(adminUserService.changeRoles(id, request));
    }
}
