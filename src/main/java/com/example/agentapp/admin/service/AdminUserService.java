package com.example.agentapp.admin.service;

import com.example.agentapp.admin.dto.AdminUserDTO;
import com.example.agentapp.admin.dto.ChangeRoleRequest;
import com.example.agentapp.admin.dto.CreateStaffRequest;
import com.example.agentapp.auth.model.Role;
import com.example.agentapp.auth.model.RoleName;
import com.example.agentapp.auth.model.User;
import com.example.agentapp.auth.model.UserType;
import com.example.agentapp.auth.repository.RoleRepository;
import com.example.agentapp.auth.repository.UserRepository;
import com.example.agentapp.payment.repository.PaymentRepository;
import com.example.agentapp.reservation.model.ReservationStatus;
import com.example.agentapp.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private static final long INVITE_TTL_DAYS = 7;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminUserService(UserRepository userRepository,
                             RoleRepository roleRepository,
                             ReservationRepository reservationRepository,
                             PaymentRepository paymentRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AdminUserDTO getUserById(Long id) {
        User user = findUser(id);
        return toDTO(user);
    }

    @Transactional
    public AdminUserDTO setUserStatus(Long id, boolean enabled) {
        User user = findUser(id);
        user.setEnabled(enabled);
        return toDTO(userRepository.save(user));
    }

    @Transactional
    public AdminUserDTO unlockUser(Long id) {
        User user = findUser(id);
        user.unlock();
        return toDTO(userRepository.save(user));
    }

    @Transactional
    public AdminUserDTO changeRoles(Long id, ChangeRoleRequest request) {
        User user = findUser(id);

        Set<Role> newRoles = resolveRoles(request.getRoles());
        user.setRoles(newRoles);

        // Keep userType aligned with the user's most-privileged role
        user.setUserType(deriveUserType(newRoles));

        return toDTO(userRepository.save(user));
    }

    // ============================================================
    // STAFF CREATION + INVITE FLOW
    // ============================================================

    /**
     * Provision a STAFF account in PENDING state: random throwaway password,
     * one-time invite token (7-day TTL). The new hire activates by setting
     * their own password via the activation link.
     */
    @Transactional
    public AdminUserDTO createStaff(CreateStaffRequest request) {
        if (Boolean.TRUE.equals(userRepository.existsByUsername(request.getUsername()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Username already in use: " + request.getUsername());
        }
        if (Boolean.TRUE.equals(userRepository.existsByEmail(request.getEmail()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email already in use: " + request.getEmail());
        }

        Set<Role> roles = resolveRoles(request.getRoles());
        if (roles.stream().anyMatch(r -> RoleName.CUSTOMER.name().equals(r.getName()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot invite a STAFF account with CUSTOMER role");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRoles(roles);
        user.setUserType(deriveUserType(roles));
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setEmailVerified(false); // they activate by clicking the invite

        // Random throwaway — unusable until they set their own through /api/auth/activate
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        issueInvite(user);
        return toDTO(userRepository.save(user));
    }

    @Transactional
    public AdminUserDTO resendInvite(Long id) {
        User user = findUser(id);
        if (user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Account is already activated; cannot resend invite");
        }
        issueInvite(user);
        return toDTO(userRepository.save(user));
    }

    private void issueInvite(User user) {
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        user.setInviteToken(token);
        user.setInviteTokenExpiresAt(LocalDateTime.now().plusDays(INVITE_TTL_DAYS));
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    private Set<Role> resolveRoles(Iterable<String> requestedRoleNames) {
        Set<Role> resolved = new HashSet<>();
        for (String roleName : requestedRoleNames) {
            RoleName rn;
            try {
                rn = RoleName.valueOf(roleName.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleName);
            }
            Role role = roleRepository.findByName(rn.name())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + roleName));
            resolved.add(role);
        }
        if (resolved.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one role is required");
        }
        return resolved;
    }

    /**
     * Map a role set to the legacy {@link UserType} field by picking the most
     * privileged role present. SUPER_ADMIN collapses to {@code ADMIN}.
     */
    private UserType deriveUserType(Set<Role> roles) {
        Set<String> names = roles.stream().map(Role::getName).collect(Collectors.toSet());
        if (names.contains(RoleName.SUPER_ADMIN.name())) return UserType.ADMIN;
        if (names.contains(RoleName.MANAGER.name())) return UserType.MANAGER;
        if (names.contains(RoleName.OFFICER.name())) return UserType.OFFICER;
        if (names.contains(RoleName.AGENT.name())) return UserType.AGENT;
        return UserType.CUSTOMER;
    }

    private AdminUserDTO toDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(trim(user.getFirstName()) + " " + trim(user.getLastName()));
        dto.setPhone(user.getPhone());
        dto.setUserType(user.getUserType().name());
        dto.setEnabled(user.isEnabled());
        dto.setLocked(user.isLocked());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());

        // Account status + invite token only meaningful for staff awaiting activation
        boolean pending = !user.isEmailVerified() && user.hasActiveInvite();
        dto.setAccountStatus(pending ? "PENDING" : "ACTIVE");
        dto.setInviteToken(pending ? user.getInviteToken() : null);

        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoles(roleNames);

        // Enriched metrics
        Long userId = user.getId();
        dto.setTotalReservations(reservationRepository.countByUserId(userId));
        dto.setConfirmedReservations(reservationRepository.countByUserIdAndReservationStatus(userId, ReservationStatus.CONFIRMED));
        dto.setCancelledReservations(reservationRepository.countByUserIdAndReservationStatus(userId, ReservationStatus.CANCELLED));

        // Total spent: sum of all completed payments on this user's reservations
        double totalSpent = reservationRepository.findByUserId(userId).stream()
                .mapToDouble(r -> paymentRepository.sumCompletedPaymentsByReservationId(r.getId()).doubleValue())
                .sum();
        dto.setTotalSpent(java.math.BigDecimal.valueOf(totalSpent));

        return dto;
    }

    private String trim(String s) {
        return s != null ? s : "";
    }
}
