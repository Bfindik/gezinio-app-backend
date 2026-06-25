package com.example.agentapp.auth.service;

import com.example.agentapp.auth.dto.AcceptInviteRequest;
import com.example.agentapp.auth.dto.AuthResponse;
import com.example.agentapp.auth.dto.InvitePreviewDTO;
import com.example.agentapp.auth.dto.RegisterRequest;
import com.example.agentapp.auth.model.*;
import com.example.agentapp.auth.repository.RoleRepository;
import com.example.agentapp.auth.repository.UserRepository;
import com.example.agentapp.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock RefreshTokenService refreshTokenService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks AuthService service;

    @Test
    void register_whenDuplicateUsername_throwsRuntimeException() {
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.register(registerRequest("taken", "new@mail.com")));

        assertTrue(ex.getMessage().contains("Username already exists"));
    }

    @Test
    void register_whenDuplicateEmail_throwsRuntimeException() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@mail.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.register(registerRequest("newuser", "taken@mail.com")));

        assertTrue(ex.getMessage().contains("Email already exists"));
    }

    @Test
    void register_success_savesUserAndPublishesEvent() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$hashed");

        Role customerRole = new Role("CUSTOMER");
        when(roleRepository.findByName(RoleName.CUSTOMER.name())).thenReturn(Optional.of(customerRole));

        User savedUser = new User("newuser", "new@mail.com", "$hashed");
        savedUser.setId(1L);
        savedUser.setUserType(UserType.CUSTOMER);
        savedUser.getRoles().add(customerRole);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        RefreshToken token = new RefreshToken();
        token.setToken("refresh-token-value");
        token.setUser(savedUser);
        when(refreshTokenService.createRefreshToken(any())).thenReturn(token);
        when(jwtService.generateToken(anyString(), any())).thenReturn("access-token");
        when(jwtService.getJwtExpirationInSeconds()).thenReturn(86400L);

        service.register(registerRequest("newuser", "new@mail.com"));

        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publishEvent(any());
    }

    // ─── peekInvite ───────────────────────────────────────────────────────────

    @Test
    void peekInvite_whenTokenUnknown_throwsNotFound() {
        when(userRepository.findByInviteToken("nope")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.peekInvite("nope"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void peekInvite_whenTokenExpired_throwsGone() {
        User user = invitedUser("expired-token", LocalDateTime.now().minusDays(1));
        when(userRepository.findByInviteToken("expired-token")).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.peekInvite("expired-token"));

        assertEquals(HttpStatus.GONE, ex.getStatusCode());
    }

    @Test
    void peekInvite_whenValid_returnsPreviewWithoutConsuming() {
        User user = invitedUser("good-token", LocalDateTime.now().plusDays(5));
        when(userRepository.findByInviteToken("good-token")).thenReturn(Optional.of(user));

        InvitePreviewDTO preview = service.peekInvite("good-token");

        assertEquals("invitee", preview.getUsername());
        assertEquals("Beyza", preview.getFirstName());
        assertEquals("invitee@mail.com", preview.getEmail());
        assertEquals("activate", preview.getType());
        // Token MUST still be set after a peek — peek does not consume.
        verify(userRepository, never()).save(any());
        assertEquals("good-token", user.getInviteToken());
    }

    // ─── activateAccount ──────────────────────────────────────────────────────

    @Test
    void activate_whenTokenUnknown_throwsNotFound() {
        when(userRepository.findByInviteToken("nope")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.activateAccount(acceptInvite("nope", "Strong1!")));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void activate_whenTokenExpired_throwsGone() {
        User user = invitedUser("expired", LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByInviteToken("expired")).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.activateAccount(acceptInvite("expired", "Strong1!")));

        assertEquals(HttpStatus.GONE, ex.getStatusCode());
    }

    @Test
    void activate_success_setsPasswordClearsTokenAndIssuesSession() {
        User user = invitedUser("good", LocalDateTime.now().plusDays(3));
        when(userRepository.findByInviteToken("good")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("BrandNew1!")).thenReturn("$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(eq("invitee"), anyLong())).thenReturn("jwt-token");
        when(jwtService.getJwtExpirationInSeconds()).thenReturn(86400L);
        RefreshToken rt = new RefreshToken();
        rt.setToken("refresh-token");
        rt.setUser(user);
        when(refreshTokenService.createRefreshToken(user)).thenReturn(rt);

        AuthResponse response = service.activateAccount(acceptInvite("good", "BrandNew1!"));

        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("$hashed", saved.getPassword());
        assertTrue(saved.isEmailVerified());
        assertTrue(saved.isEnabled());
        assertNull(saved.getInviteToken(), "invite token must be one-shot");
        assertNull(saved.getInviteTokenExpiresAt());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private RegisterRequest registerRequest(String username, String email) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword("password123");
        req.setFirstName("Test");
        req.setLastName("User");
        return req;
    }

    private AcceptInviteRequest acceptInvite(String token, String password) {
        AcceptInviteRequest req = new AcceptInviteRequest();
        req.setToken(token);
        req.setPassword(password);
        return req;
    }

    private User invitedUser(String token, LocalDateTime expiresAt) {
        User user = new User("invitee", "invitee@mail.com", "$throwaway");
        user.setId(42L);
        user.setUserType(UserType.OFFICER);
        user.setFirstName("Beyza");
        user.setLastName("Fındık");
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setInviteToken(token);
        user.setInviteTokenExpiresAt(expiresAt);
        return user;
    }
}
