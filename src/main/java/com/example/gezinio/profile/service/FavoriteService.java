package com.example.gezinio.profile.service;

import com.example.gezinio.auth.model.User;
import com.example.gezinio.auth.repository.UserRepository;
import com.example.gezinio.excursion.model.Tour;
import com.example.gezinio.excursion.repository.TourRepository;
import com.example.gezinio.profile.dto.FavoriteDTO;
import com.example.gezinio.profile.model.UserFavorite;
import com.example.gezinio.profile.repository.UserFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final UserFavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;

    @Autowired
    public FavoriteService(UserFavoriteRepository favoriteRepository,
                            UserRepository userRepository,
                            TourRepository tourRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
    }

    public List<FavoriteDTO> getMyFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public FavoriteDTO addFavorite(Long userId, Long tourId) {
        if (favoriteRepository.existsByUserIdAndTourId(userId, tourId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tour is already in your favorites");
        }

        User user = findUser(userId);
        Tour tour = findTour(tourId);

        UserFavorite favorite = new UserFavorite(user, tour);
        return toDTO(favoriteRepository.save(favorite));
    }

    @Transactional
    public void removeFavorite(Long userId, Long tourId) {
        if (!favoriteRepository.existsByUserIdAndTourId(userId, tourId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found in your favorites");
        }
        favoriteRepository.deleteByUserIdAndTourId(userId, tourId);
    }

    public boolean isFavorite(Long userId, Long tourId) {
        return favoriteRepository.existsByUserIdAndTourId(userId, tourId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Tour findTour(Long tourId) {
        return tourRepository.findById(tourId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found: " + tourId));
    }

    private FavoriteDTO toDTO(UserFavorite f) {
        FavoriteDTO dto = new FavoriteDTO();
        dto.setId(f.getId());
        Tour tour = f.getTour();
        dto.setTourId(tour.getId());
        dto.setTourName(tour.getName());
        dto.setDestination(tour.getDestination());
        dto.setTourType(tour.getTourType().name());
        dto.setTourStatus(tour.getTourStatus().name());
        dto.setPrice(tour.getPrice());
        dto.setCurrency(tour.getCurrency());
        dto.setDurationDays(tour.getDurationDays());
        dto.setAddedAt(f.getCreatedAt());
        return dto;
    }
}
