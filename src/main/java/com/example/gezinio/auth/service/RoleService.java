package com.example.gezinio.auth.service;

import com.example.gezinio.auth.model.Permission;
import com.example.gezinio.auth.model.PermissionName;
import com.example.gezinio.auth.model.Role;
import com.example.gezinio.auth.model.RoleName;
import com.example.gezinio.auth.repository.PermissionRepository;
import com.example.gezinio.auth.repository.RoleRepository;
import com.example.gezinio.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final UserRepository userRepository;

    private final UserService userService;

    @Autowired
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, UserRepository userRepository, UserService userService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    @Transactional
    public Role createRole(
            String name,
            String description,
            Set<PermissionName> permissionNames) {

        // Name unique mi?
        if (roleRepository.existsByName(name)) {
            throw new RuntimeException("Role name already exists: " + name);
        }

        // Sistem rolü adı kullanılıyor mu?
        try {
            RoleName.valueOf(name);
            throw new RuntimeException(
                    "Cannot use system role name: " + name
            );
        } catch (IllegalArgumentException e) {
            // OK - Sistem rolü değil
        }


        Role role = new Role();
        role.setName(name.toUpperCase()); // Uppercase normalize
        role.setDescription(description);
        role.setSystemRole(false); // ← ÖZEL ROL

        // Created by
        Long currentUserId = userService.getCurrentUser().getId();
        role.setCreatedBy(currentUserId);

        if (permissionNames != null && !permissionNames.isEmpty()) {
            Set<Permission> permissions = permissionNames.stream()
                    .map(permissionName ->
                            permissionRepository.findByName(permissionName)
                                    .orElseThrow(() -> new RuntimeException(
                                            "Permission not found: " + permissionName
                                    ))
                    )
                    .collect(Collectors.toSet());

            role.setPermissions(permissions);
        }

        return roleRepository.save(role);
    }

    @Transactional
    public Role updateRole(
            Long roleId,
            String newName,
            String newDescription) {

        Role role = getRoleById(roleId);

        if (newName != null && !newName.equals(role.getName())) {

            // Sistem rolünün adı değiştirilemez
            if (role.isSystemRole()) {
                throw new RuntimeException(
                        "Cannot change system role name"
                );
            }

            // Duplicate check
            if (roleRepository.existsByName(newName)) {
                throw new RuntimeException(
                        "Role name already exists: " + newName
                );
            }

            role.setName(newName.toUpperCase());
        }


        if (newDescription != null) {
            role.setDescription(newDescription);
        }

        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(Long roleId) {

        Role role = getRoleById(roleId);

        if (role.isSystemRole()) {
            throw new RuntimeException(
                    "Cannot delete system role: " + role.getName()
            );
        }

         long userCount = userRepository.countByRolesContaining(role);
         if (userCount > 0) {
             throw new RuntimeException(
                 "Cannot delete role in use by " + userCount + " users"
             );
         }

        roleRepository.delete(role);
    }

    @Transactional
    public Role addPermissionsToRole(
            Long roleId,
            Set<PermissionName> permissionNames) {

        Role role = getRoleById(roleId);

        for (PermissionName permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new RuntimeException(
                            "Permission not found: " + permissionName
                    ));

            role.addPermission(permission);
        }

        return roleRepository.save(role);
    }

    @Transactional
    public Role removePermissionsFromRole(
            Long roleId,
            Set<PermissionName> permissionNames) {

        Role role = getRoleById(roleId);

        for (PermissionName permissionName : permissionNames) {
            Permission permission = permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new RuntimeException(
                            "Permission not found: " + permissionName
                    ));

            role.removePermission(permission);
        }

        return roleRepository.save(role);
    }

    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {

        // User bul
        var user = userService.getUserById(userId);

        // Role bul
        Role role = getRoleById(roleId);

        // User entity'ye role ekle
        // user.getRoles().add(role);
        // userRepository.save(user);

        // TODO: UserService'e assignRole() metodu ekle
    }
}