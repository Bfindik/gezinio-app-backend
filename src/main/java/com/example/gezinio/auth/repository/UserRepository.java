package com.example.gezinio.auth.repository;
import com.example.gezinio.auth.model.Role;
import com.example.gezinio.auth.model.User;
import com.example.gezinio.auth.model.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    Optional<User> findByInviteToken(String inviteToken);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    List<User> findByUserType(UserType userType);

    List<User> findByEnabled(boolean enabled);

    List<User> findByEmailVerified(boolean emailVerified);

    List<User> findByAccountNonLocked(boolean accountNonLocked);

    List<User> findByCreatedAtAfter(LocalDateTime date);

    List<User> findByLastLoginAfter(LocalDateTime date);

    @Query("SELECT u FROM User u WHERE u.accountNonLocked = false " +
            "AND u.lockTime IS NOT NULL " +
            "AND u.lockTime < :unlockTime")
    List<User> findLockedUsersToUnlock(@Param("unlockTime") LocalDateTime unlockTime);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(:username)")
    List<User> searchByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(:email)")
    List<User> searchByEmail(@Param("email") String email);

    @Query("SELECT COUNT(DISTINCT u) FROM User u " +
            "JOIN u.roles r " +
            "WHERE r.name IN ('SUPER_ADMIN', 'MANAGER')")
    long countUsersWithAdminRole();

    long countByRolesContaining(Role role);

    long countByUserType(UserType userType);

    long countByEnabled(boolean enabled);
}

