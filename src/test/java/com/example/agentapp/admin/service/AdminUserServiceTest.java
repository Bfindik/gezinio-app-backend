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
import com.example.agentapp.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// The @BeforeEach stubs toDTO inputs that are only used by happy-path tests.
// LENIENT strictness keeps the early-throw cases (NotFound / Conflict / BadRequest)
// from failing on unused stubs — those tests intentionally never reach toDTO.
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminUserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock PasswordEncoder passwordEncoder;

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
        when(roleRepository.findByName(RoleName.MANAGER.name())).thenReturn(Optional.empty());

        ChangeRoleRequest req = new ChangeRoleRequest();
        req.setRoles(Set.of("MANAGER"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.changeRoles(1L, req));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void changeRoles_success_updatesUserRoles() {
        Role managerRole = new Role("MANAGER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(RoleName.MANAGER.name())).thenReturn(Optional.of(managerRole));
        when(userRepository.save(any())).thenReturn(user);

        ChangeRoleRequest req = new ChangeRoleRequest();
        req.setRoles(Set.of("MANAGER"));

        service.changeRoles(1L, req);

        assertTrue(user.getRoles().contains(managerRole));
        verify(userRepository).save(user);
    }

    // ─── createStaff ──────────────────────────────────────────────────────────

    @Test
    void createStaff_whenUsernameTaken_throwsConflict() {
        when(userRepository.existsByUsername("ali")).thenReturn(true);

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.createStaff(staffRequest("ali", "ali@mail.com", "OFFICER")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createStaff_whenCustomerRoleRequested_throwsBadRequest() {
        when(userRepository.existsByUsername("ali")).thenReturn(false);
        when(userRepository.existsByEmail("ali@mail.com")).thenReturn(false);
        Role customerRole = new Role(RoleName.CUSTOMER.name());
        when(roleRepository.findByName(RoleName.CUSTOMER.name())).thenReturn(Optional.of(customerRole));

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.createStaff(staffRequest("ali", "ali@mail.com", "CUSTOMER")));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createStaff_success_provisionsPendingUserWithInviteToken() {
        when(userRepository.existsByUsername("ali")).thenReturn(false);
        when(userRepository.existsByEmail("ali@mail.com")).thenReturn(false);
        Role officerRole = new Role(RoleName.OFFICER.name());
        when(roleRepository.findByName(RoleName.OFFICER.name())).thenReturn(Optional.of(officerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("$throwaway");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(7L);
            return u;
        });
        when(reservationRepository.countByUserId(7L)).thenReturn(0L);
        when(reservationRepository.findByUserId(7L)).thenReturn(List.of());

        AdminUserDTO dto = service.createStaff(staffRequest("ali", "ali@mail.com", "OFFICER"));

        assertEquals("ali", dto.getUsername());
        assertEquals("PENDING", dto.getAccountStatus());
        assertNotNull(dto.getInviteToken(), "invite token must be present for pending account");
        assertFalse(dto.isEmailVerified());
    }

    // ─── resendInvite ─────────────────────────────────────────────────────────

    @Test
    void resendInvite_whenAlreadyActivated_throwsBadRequest() {
        user.setEmailVerified(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        org.springframework.web.server.ResponseStatusException ex = assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.resendInvite(1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void resendInvite_success_mintsNewToken() {
        user.setEmailVerified(false);
        user.setInviteToken("old-token");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminUserDTO dto = service.resendInvite(1L);

        assertNotNull(dto.getInviteToken());
        assertNotEquals("old-token", dto.getInviteToken());
        assertEquals("PENDING", dto.getAccountStatus());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private CreateStaffRequest staffRequest(String username, String email, String role) {
        CreateStaffRequest req = new CreateStaffRequest();
        req.setFirstName("Ali");
        req.setLastName("Veli");
        req.setUsername(username);
        req.setEmail(email);
        req.setPhone("+905551234567");
        req.setRoles(Set.of(role));
        return req;
    }
}
