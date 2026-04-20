package com.example.agentapp.auth.service;

import com.example.agentapp.auth.dto.RegisterRequest;
import com.example.agentapp.auth.model.*;
import com.example.agentapp.auth.repository.RoleRepository;
import com.example.agentapp.auth.repository.UserRepository;
import com.example.agentapp.auth.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        when(roleRepository.findByName(RoleName.CUSTOMER)).thenReturn(Optional.of(customerRole));

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

    // ─── Helper ───────────────────────────────────────────────────────────────

    private RegisterRequest registerRequest(String username, String email) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword("password123");
        req.setFirstName("Test");
        req.setLastName("User");
        return req;
    }
}
