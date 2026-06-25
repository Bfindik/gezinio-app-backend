package com.example.gezinio.auth.repository;

import com.example.gezinio.auth.model.Permission;
import com.example.gezinio.auth.model.PermissionName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(PermissionName name);
    boolean existsByName(PermissionName name);
    List<Permission> findByNameIn(Set<PermissionName> names);
}
