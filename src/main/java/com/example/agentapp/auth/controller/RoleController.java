package com.example.agentapp.auth.controller;

import com.example.agentapp.auth.model.PermissionName;
import com.example.agentapp.auth.model.Role;
import com.example.agentapp.auth.service.RoleService;
import com.example.agentapp.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('SUPER_ADMIN')") // ← Tüm endpoint'ler SUPER_ADMIN gerektirir
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Role Management", description = "Manage roles and their permission assignments (SUPER_ADMIN only)")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @Operation(summary = "List all roles")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(
                ApiResponse.success("Roles retrieved successfully", roles)
        );
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get a role by its ID")
    public ResponseEntity<ApiResponse<Role>> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Role retrieved successfully", role)
        );
    }

    @PostMapping
    @Operation(summary = "Create a new role with the given permissions")
    public ResponseEntity<ApiResponse<Role>> createRole(
            @RequestBody CreateRoleRequest request) {

        Role role = roleService.createRole(
                request.getName(),
                request.getDescription(),
                request.getPermissions()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Role created successfully", role));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update the name or description of a role")
    public ResponseEntity<ApiResponse<Role>> updateRole(
            @PathVariable Long id,
            @RequestBody UpdateRoleRequest request) {

        Role role = roleService.updateRole(
                id,
                request.getName(),
                request.getDescription()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Role updated successfully", role)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a role by its ID")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {

        roleService.deleteRole(id);

        return ResponseEntity.ok(
                ApiResponse.success("Role deleted successfully")
        );
    }

    @PostMapping("/{id}/permissions/add")
    @Operation(summary = "Add one or more permissions to a role")
    public ResponseEntity<ApiResponse<Role>> addPermissions(
            @PathVariable Long id,
            @RequestBody PermissionRequest request) {

        Role role = roleService.addPermissionsToRole(
                id,
                request.getPermissions()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Permissions added successfully", role)
        );
    }


    @PostMapping("/{id}/permissions/remove")
    @Operation(summary = "Remove one or more permissions from a role")
    public ResponseEntity<ApiResponse<Role>> removePermissions(
            @PathVariable Long id,
            @RequestBody PermissionRequest request) {

        Role role = roleService.removePermissionsFromRole(
                id,
                request.getPermissions()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Permissions removed successfully", role)
        );
    }

    public static class CreateRoleRequest {
        private String name;
        private String description;
        private Set<PermissionName> permissions;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Set<PermissionName> getPermissions() { return permissions; }
        public void setPermissions(Set<PermissionName> permissions) { this.permissions = permissions; }
    }

    public static class UpdateRoleRequest {
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class PermissionRequest {
        private Set<PermissionName> permissions;

        public Set<PermissionName> getPermissions() { return permissions; }
        public void setPermissions(Set<PermissionName> permissions) { this.permissions = permissions; }
    }
}