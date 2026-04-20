package com.example.agentapp.profile.service;

import com.example.agentapp.auth.model.User;
import com.example.agentapp.auth.repository.UserRepository;
import com.example.agentapp.profile.dto.ChangePasswordRequest;
import com.example.agentapp.profile.dto.UpdateProfileRequest;
import com.example.agentapp.profile.repository.TourReviewRepository;
import com.example.agentapp.profile.repository.UserFavoriteRepository;
import com.example.agentapp.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ReservationRepository reservationRepository;
    @Mock UserFavoriteRepository favoriteRepository;
    @Mock TourReviewRepository reviewRepository;

    @InjectMocks UserProfileService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("tourist", "tourist@mail.com", "$2a$bcryptHash");
        user.setId(1L);
    }

    // ─── changePassword ───────────────────────────────────────────────────────

    @Test
    void changePassword_whenWrongCurrentPassword_throwsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", user.getPassword())).thenReturn(false);

        ChangePasswordRequest req = changePasswordRequest("wrongpass", "newPass123", "newPass123");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.changePassword(1L, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Current password is incorrect"));
    }

    @Test
    void changePassword_whenNewPasswordsMismatch_throwsBadRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPass", user.getPassword())).thenReturn(true);

        ChangePasswordRequest req = changePasswordRequest("currentPass", "newPass123", "different456");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.changePassword(1L, req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("do not match"));
    }

    @Test
    void changePassword_success_encodesAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("currentPass", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("$2a$newHash");
        when(userRepository.save(any())).thenReturn(user);

        service.changePassword(1L, changePasswordRequest("currentPass", "newPass123", "newPass123"));

        verify(passwordEncoder).encode("newPass123");
        verify(userRepository).save(user);
    }

    // ─── updateProfile ────────────────────────────────────────────────────────

    @Test
    void updateProfile_updatesOnlyProvidedFields() {
        user.setFirstName("Old");
        user.setLastName("Name");
        user.setPhone("000");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(reservationRepository.countByUserId(1L)).thenReturn(0L);
        when(reservationRepository.countByUserIdAndReservationStatus(any(), any())).thenReturn(0L);
        when(favoriteRepository.countByUserId(1L)).thenReturn(0L);
        when(reviewRepository.findByUserId(1L)).thenReturn(List.of());

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setFirstName("New");
        // lastName and phone not set → should remain unchanged

        service.updateProfile(1L, req);

        assertEquals("New", user.getFirstName());
        assertEquals("Name", user.getLastName()); // unchanged
        assertEquals("000", user.getPhone());      // unchanged
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private ChangePasswordRequest changePasswordRequest(String current, String newPwd, String confirm) {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword(current);
        req.setNewPassword(newPwd);
        req.setConfirmPassword(confirm);
        return req;
    }
}
