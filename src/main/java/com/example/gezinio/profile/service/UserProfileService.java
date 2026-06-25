package com.example.gezinio.profile.service;

import com.example.gezinio.auth.model.Role;
import com.example.gezinio.auth.model.User;
import com.example.gezinio.auth.repository.UserRepository;
import com.example.gezinio.profile.dto.ChangePasswordRequest;
import com.example.gezinio.profile.dto.UpdateProfileRequest;
import com.example.gezinio.profile.dto.UserProfileDTO;
import com.example.gezinio.profile.repository.TourReviewRepository;
import com.example.gezinio.profile.repository.UserFavoriteRepository;
import com.example.gezinio.reservation.model.ReservationStatus;
import com.example.gezinio.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReservationRepository reservationRepository;
    private final UserFavoriteRepository favoriteRepository;
    private final TourReviewRepository reviewRepository;

    @Autowired
    public UserProfileService(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               ReservationRepository reservationRepository,
                               UserFavoriteRepository favoriteRepository,
                               TourReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.reservationRepository = reservationRepository;
        this.favoriteRepository = favoriteRepository;
        this.reviewRepository = reviewRepository;
    }

    public UserProfileDTO getProfile(Long userId) {
        return toDTO(findUser(userId));
    }

    @Transactional
    public UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUser(userId);

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());

        return toDTO(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUser(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserProfileDTO toDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setFullName(nullSafe(user.getFirstName()) + " " + nullSafe(user.getLastName()));
        dto.setPhone(user.getPhone());
        dto.setUserType(user.getUserType().name());
        dto.setEmailVerified(user.isEmailVerified());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoles(roles);

        Long id = user.getId();
        dto.setTotalReservations(reservationRepository.countByUserId(id));
        dto.setConfirmedReservations(reservationRepository.countByUserIdAndReservationStatus(id, ReservationStatus.CONFIRMED));
        dto.setCancelledReservations(reservationRepository.countByUserIdAndReservationStatus(id, ReservationStatus.CANCELLED));
        dto.setTotalFavorites(favoriteRepository.countByUserId(id));
        dto.setTotalReviews(reviewRepository.findByUserId(id).size());

        return dto;
    }

    private String nullSafe(String s) {
        return s != null ? s : "";
    }
}
