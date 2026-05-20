package com.example.agentapp.admin.service;

import com.example.agentapp.admin.dto.AdminUserDTO;
import com.example.agentapp.admin.dto.ChangeRoleRequest;
import com.example.agentapp.auth.model.Role;
import com.example.agentapp.auth.model.RoleName;
import com.example.agentapp.auth.model.User;
import com.example.agentapp.auth.repository.RoleRepository;
import com.example.agentapp.auth.repository.UserRepository;
import com.example.agentapp.payment.repository.PaymentRepository;
import com.example.agentapp.reservation.model.ReservationStatus;
import com.example.agentapp.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public AdminUserService(UserRepository userRepository,
                             RoleRepository roleRepository,
                             ReservationRepository reservationRepository,
                             PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
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

        Set<Role> newRoles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            RoleName rn;
            try {
                rn = RoleName.valueOf(roleName.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleName);
            }
            Role role = roleRepository.findByName(rn.name())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + roleName));
            newRoles.add(role);
        }

        user.setRoles(newRoles);
        return toDTO(userRepository.save(user));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
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
