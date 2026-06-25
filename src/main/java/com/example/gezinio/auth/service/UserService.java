package com.example.gezinio.auth.service;

import com.example.gezinio.auth.dto.ChangePasswordRequest;
import com.example.gezinio.auth.dto.UserDTO;
import com.example.gezinio.auth.model.User;
import com.example.gezinio.auth.repository.UserRepository;
import com.example.gezinio.auth.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        // Null check (defensive programming)
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        // fresh data
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: " + userPrincipal.getId()
                ));

        return mapToDTO(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return mapToDTO(user);
    }

    @Transactional
    public UserDTO updateProfile(Long userId, String firstName, String lastName, String phone) {
        // sadece aynı user değiştirebilir
        UserDTO currentUser = getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            throw new RuntimeException(
                    "Unauthorized: You can only update your own profile"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName);
        }

        if (lastName != null && !lastName.isBlank()) {
            user.setLastName(lastName);
        }

        if (phone != null && !phone.isBlank()) {
            user.setPhone(phone);
        }

        User updatedUser = userRepository.save(user);

        return mapToDTO(updatedUser);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {

        UserDTO currentUser = getCurrentUser();

        if (!currentUser.getId().equals(userId)) {
            throw new RuntimeException(
                    "Unauthorized: You can only change your own password"
            );
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOldPasswordCorrect = passwordEncoder.matches(
                request.getCurrentPassword(), // Düz text
                user.getPassword()            // Hash
        );

        if (!isOldPasswordCorrect) {
            throw new RuntimeException("Current password is incorrect");
        }

        String newHashedPassword = passwordEncoder.encode(request.getNewPassword());

        user.setPassword(newHashedPassword);

        userRepository.save(user);
        refreshTokenService.deleteByUser(user); // güvenlik için
    }

    // admin dashboard için
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // admin için kullanıcı silme
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserDTO currentUser = getCurrentUser();
        if (currentUser.getId().equals(userId)) {
            throw new RuntimeException("You cannot delete your own account");
        }

        validateNotLastAdmin(user);

        refreshTokenService.deleteByUser(user);

        userRepository.delete(user);

    }
    private void validateNotLastAdmin(User user) {

        // Admin mi kontrol et
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role ->
                        role.getName() == "SUPER_ADMIN" ||
                                role.getName() == "MANAGER"
                );

        if (!isAdmin) {
            return; // Admin değilse sorun yok
        }

        long adminCount = userRepository.countUsersWithAdminRole();

        if (adminCount <= 1) {
            throw new RuntimeException(
                    "Cannot delete the last admin user. " +
                            "Please assign another admin first."
            );
        }
    }
    private void performSoftDelete(User user) {
        // Devre dışı bırak
        user.setEnabled(false);

        // GDPR: Email maskele
        user.setEmail("deleted_" + user.getId() + "@deleted.com");

        // İLERİDE: deletedAt field ekle
        // user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);
    }
    @Transactional
    public void softDeleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        performSoftDelete(user);
    }
    @Transactional
    public void deleteOwnAccount(String passwordConfirmation) {

        UserDTO currentUserDTO = getCurrentUser();
        User user = userRepository.findById(currentUserDTO.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // silmek için şifre al
        if (passwordConfirmation != null) {
            boolean isPasswordCorrect = passwordEncoder.matches(
                    passwordConfirmation,
                    user.getPassword()
            );

            if (!isPasswordCorrect) {
                throw new RuntimeException("Password confirmation failed");
            }
        }

        validateNotLastAdmin(user);
        refreshTokenService.deleteByUser(user);
        performSoftDelete(user);
        // email gönderme eklenecek
    }

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

        // Roles (String set)
        dto.setRoles(
                user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet())
        );


        return dto;
    }
}