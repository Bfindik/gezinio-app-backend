package com.example.agentapp.admin.service;

import com.example.agentapp.admin.dto.AdminUserDTO;
import com.example.agentapp.admin.dto.ChangeRoleRequest;
import com.example.agentapp.auth.model.Role;
import com.example.agentapp.auth.model.RoleName;
import com.example.agentapp.auth.model.User;
import com.example.agentapp.auth.model.UserType;
import com.example.agentapp.auth.repository.RoleRepository;
import com.example.agentapp.auth.repository.UserRepository;
import com.example.agentapp.payment.repository.PaymentRepository;
import com.example.agentapp.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock PaymentRepository paymentRepository;

    @InjectMocks AdminUserService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("tourist", "tourist@mail.com", "hash");
        user.setId(1L);
        user.setUserType(UserType.CUSTOMER);
        user.setEnabled(true);

        // stub metrics used in toDTO
        when(reservationRepository.countByUserId(1L)).thenReturn(0L);
        when(reservationRepository.countByUserIdAndReservationStatus(any(), any())).thenReturn(0L);
        when(reservationRepository.findByUserId(1L)).thenReturn(List.of());
    }

    // ─── getUserById ──────────────────────────────────────────────────────────

    @Test
    void getUserById_whenNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getUserById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getUserById_success_returnsDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminUserDTO dto = service.getUserById(1L);

        assertEquals("tourist", dto.getUsername());
        assertTrue(dto.isEnabled());
    }

    // ─── setUserStatus ────────────────────────────────────────────────────────

    @Test
    void setUserStatus_disable_setsEnabledFalse() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        service.setUserStatus(1L, false);

        assertFalse(user.isEnabled());
        verify(userRepository).save(user);
    }

    @Test
    void setUserStatus_enable_setsEnabledTrue() {
        user.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        service.setUserStatus(1L, true);

        assertTrue(user.isEnabled());
    }

    // ─── unlockUser ───────────────────────────────────────────────────────────

    @Test
    void unlockUser_whenNotFound_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.unlockUser(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void unlockUser_success_callsUnlockAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        service.unlockUser(1L);

        verify(userRepository).save(user);
    }

    // ─── changeRoles ──────────────────────────────────────────────────────────

    @Test
    void changeRoles_whenInvalidRoleName_throwsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ChangeRoleRequest req = new ChangeRoleRequest();
        req.setRoles(Set.of("INVALID_ROLE"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.changeRoles(1L, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void changeRoles_whenRoleNotInDB_throwsNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.empty());

        ChangeRoleRequest req = new ChangeRoleRequest();
        req.setRoles(Set.of("ADMIN"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.changeRoles(1L, req));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void changeRoles_success_updatesUserRoles() {
        Role adminRole = new Role("ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any())).thenReturn(user);

        ChangeRoleRequest req = new ChangeRoleRequest();
        req.setRoles(Set.of("ADMIN"));

        service.changeRoles(1L, req);

        assertTrue(user.getRoles().contains(adminRole));
        verify(userRepository).save(user);
    }
}
