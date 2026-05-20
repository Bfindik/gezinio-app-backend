package com.example.agentapp.auth.service;

import com.example.agentapp.auth.dto.*;
import com.example.agentapp.auth.model.RefreshToken;
import com.example.agentapp.auth.model.Role;
import com.example.agentapp.auth.model.RoleName;
import com.example.agentapp.auth.model.User;
import com.example.agentapp.auth.repository.RoleRepository;
import com.example.agentapp.auth.repository.UserRepository;
import com.example.agentapp.auth.security.JwtService;
import com.example.agentapp.auth.security.UserPrincipal;
import com.example.agentapp.notification.event.AppNotificationEvent;
import com.example.agentapp.notification.model.NotificationEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public AuthService(UserRepository userRepository, RoleRepository roleRepository, RefreshTokenService refreshTokenService, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // 1. Duplicate check
        if (Boolean.TRUE.equals(userRepository.existsByUsername(request.getUsername()))) {
            throw new RuntimeException("Username already exists");
        }

        if (Boolean.TRUE.equals(userRepository.existsByEmail(request.getEmail()))) {
            throw new RuntimeException("Email already exists");
        }

        // 2. Password hash
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        // 3. User oluştur
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                hashedPassword
        );
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        // 4. Default role ekle (CUSTOMER)
        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER.name())
                .orElseThrow(() -> new RuntimeException("CUSTOMER role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(customerRole);
        user.setRoles(roles);

        // 5. Kaydet
        User savedUser = userRepository.save(user);

        // 6. Kayıt bildirimi gönder
        eventPublisher.publishEvent(new AppNotificationEvent(
                this, NotificationEventType.USER_REGISTERED,
                savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getPhone()
        ));

        // 7. Token oluştur
        String accessToken = jwtService.generateToken(
                savedUser.getUsername(),
                savedUser.getId()
        );

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        // 7. Response
        UserDTO userDTO = mapToDTO(savedUser);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtService.getJwtExpirationInSeconds(),
                userDTO
        );
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        // 1. User bul (auto-unlock için)
        User user = userRepository.findByUsernameOrEmail(
                request.getUsernameOrEmail(),
                request.getUsernameOrEmail()
        ).orElse(null);

        // Auto-unlock check
        if (user != null && user.isLocked() && user.shouldUnlock()) {
            user.unlock();
            userRepository.save(user);
        }

        // 2. Spring Security authentication
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

        } catch (BadCredentialsException ex) {
            // Login başarısız - failed attempt increment
            if (user != null) {
                user.incrementFailedAttempts();

                if (user.getFailedLoginAttempts() >= 5) {
                    user.lock();
                    userRepository.save(user);
                    throw new RuntimeException("Account locked. Try again after 30 minutes.");
                }

                userRepository.save(user);
            }

            throw new RuntimeException("Invalid username or password");
        }

        // 3. Login başarılı - reset failed attempts
        if (user != null) {
            user.resetFailedAttempts();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }

        // 4. SecurityContext'e koy
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 5. UserPrincipal al
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // 6. Token oluştur
        String accessToken = jwtService.generateToken(
                userPrincipal.getUsername(),
                userPrincipal.getId()
        );

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // 7. Response
        UserDTO userDTO = mapToDTO(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtService.getJwtExpirationInSeconds(),
                userDTO
        );
    }

    /**
     * LOGOUT
     * Çıkış - Refresh token'ları iptal et
     */
    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Refresh token'ları sil
        refreshTokenService.deleteByUser(user);

        // SecurityContext temizle
        SecurityContextHolder.clearContext();
    }

    // yeni access token al
    @Transactional
    public AuthResponse refreshToken(String refreshTokenString) {
        // 1. Refresh token validate et
        RefreshToken refreshToken = refreshTokenService
                .verifyRefreshToken(refreshTokenString);

        User user = refreshToken.getUser();

        // 2. Yeni access token oluştur
        String newAccessToken = jwtService.generateToken(
                user.getUsername(),
                user.getId()
        );

        // 3. Yeni refresh token oluştur (rotation)
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        // 4. Eski refresh token'ı revoke et
        refreshTokenService.revokeRefreshToken(refreshToken);

        // 5. Response
        UserDTO userDTO = mapToDTO(user);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                jwtService.getJwtExpirationInSeconds(),
                userDTO
        );
    }

    /**
     * HELPER METHOD: User → UserDTO
     */
    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setUserType(user.getUserType().name());
        dto.setEnabled(user.isEnabled());
        dto.setEmailVerified(user.isEmailVerified());

        // Roles
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoles(roleNames);

        return dto;
    }
}