package com.example.gezinio.profile.service;

import com.example.gezinio.auth.model.User;
import com.example.gezinio.auth.repository.UserRepository;
import com.example.gezinio.excursion.model.Tour;
import com.example.gezinio.excursion.model.TourStatus;
import com.example.gezinio.excursion.model.TourType;
import com.example.gezinio.excursion.repository.TourRepository;
import com.example.gezinio.profile.dto.FavoriteDTO;
import com.example.gezinio.profile.model.UserFavorite;
import com.example.gezinio.profile.repository.UserFavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock UserFavoriteRepository favoriteRepository;
    @Mock UserRepository userRepository;
    @Mock TourRepository tourRepository;

    @InjectMocks FavoriteService service;

    private User user;
    private Tour tour;

    @BeforeEach
    void setUp() {
        user = new User("tourist", "tourist@mail.com", "hash");
        user.setId(1L);

        tour = new Tour();
        tour.setId(10L);
        tour.setName("Istanbul Tour");
        tour.setDestination("Istanbul");
        tour.setPrice(new BigDecimal("500.00"));
        tour.setCurrency("TRY");
        tour.setTourStatus(TourStatus.ACTIVE);
        tour.setTourType(TourType.GROUP);
        tour.setDurationDays(3);
    }

    @Test
    void addFavorite_whenAlreadyFavorited_throwsConflict() {
        when(favoriteRepository.existsByUserIdAndTourId(1L, 10L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.addFavorite(1L, 10L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verifyNoMoreInteractions(userRepository, tourRepository);
    }

    @Test
    void addFavorite_success_savesAndReturnsDTO() {
        UserFavorite saved = new UserFavorite(user, tour);

        when(favoriteRepository.existsByUserIdAndTourId(1L, 10L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tourRepository.findById(10L)).thenReturn(Optional.of(tour));
        when(favoriteRepository.save(any())).thenReturn(saved);

        FavoriteDTO dto = service.addFavorite(1L, 10L);

        assertEquals("Istanbul Tour", dto.getTourName());
        verify(favoriteRepository).save(any(UserFavorite.class));
    }

    @Test
    void removeFavorite_whenNotInFavorites_throwsNotFound() {
        when(favoriteRepository.existsByUserIdAndTourId(1L, 10L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.removeFavorite(1L, 10L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void removeFavorite_success_callsDelete() {
        when(favoriteRepository.existsByUserIdAndTourId(1L, 10L)).thenReturn(true);

        service.removeFavorite(1L, 10L);

        verify(favoriteRepository).deleteByUserIdAndTourId(1L, 10L);
    }

    @Test
    void isFavorite_delegatesToRepository() {
        when(favoriteRepository.existsByUserIdAndTourId(1L, 10L)).thenReturn(true);
        assertTrue(service.isFavorite(1L, 10L));

        when(favoriteRepository.existsByUserIdAndTourId(1L, 99L)).thenReturn(false);
        assertFalse(service.isFavorite(1L, 99L));
    }
}
