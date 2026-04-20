package com.example.agentapp.profile.service;

import com.example.agentapp.auth.model.User;
import com.example.agentapp.auth.repository.UserRepository;
import com.example.agentapp.excursion.model.Tour;
import com.example.agentapp.excursion.repository.TourRepository;
import com.example.agentapp.profile.dto.CreateReviewRequest;
import com.example.agentapp.profile.dto.UpdateReviewRequest;
import com.example.agentapp.profile.model.ReviewStatus;
import com.example.agentapp.profile.model.TourReview;
import com.example.agentapp.profile.repository.TourReviewRepository;
import com.example.agentapp.reservation.model.Reservation;
import com.example.agentapp.reservation.model.ReservationStatus;
import com.example.agentapp.reservation.model.ReservationType;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock TourReviewRepository reviewRepository;
    @Mock UserRepository userRepository;
    @Mock TourRepository tourRepository;
    @Mock ReservationRepository reservationRepository;

    @InjectMocks ReviewService service;

    private User user;
    private Tour tour;

    @BeforeEach
    void setUp() {
        user = new User("tourist", "tourist@mail.com", "hash");
        user.setId(1L);

        tour = new Tour();
        tour.setId(10L);
        tour.setName("Istanbul Tour");
    }

    // ─── createReview ─────────────────────────────────────────────────────────

    @Test
    void createReview_whenNoEligibleReservation_throwsForbidden() {
        when(tourRepository.findById(10L)).thenReturn(Optional.of(tour));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        // User has no reservations for this tour
        when(reservationRepository.findByUserId(1L)).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createReview(1L, reviewRequest()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void createReview_whenReservationIsPending_throwsForbidden() {
        // Reservation exists but not confirmed/completed
        Reservation pendingReservation = buildReservation(tour, ReservationStatus.PENDING);

        when(tourRepository.findById(10L)).thenReturn(Optional.of(tour));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationRepository.findByUserId(1L)).thenReturn(List.of(pendingReservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createReview(1L, reviewRequest()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void createReview_whenAlreadyReviewed_throwsConflict() {
        Reservation confirmed = buildReservation(tour, ReservationStatus.CONFIRMED);

        when(tourRepository.findById(10L)).thenReturn(Optional.of(tour));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationRepository.findByUserId(1L)).thenReturn(List.of(confirmed));
        when(reviewRepository.existsByUserIdAndTourId(1L, 10L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createReview(1L, reviewRequest()));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createReview_success_savesPendingReview() {
        Reservation confirmed = buildReservation(tour, ReservationStatus.CONFIRMED);
        TourReview saved = buildReview(user, tour, ReviewStatus.PENDING);

        when(tourRepository.findById(10L)).thenReturn(Optional.of(tour));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationRepository.findByUserId(1L)).thenReturn(List.of(confirmed));
        when(reviewRepository.existsByUserIdAndTourId(1L, 10L)).thenReturn(false);
        when(reviewRepository.save(any())).thenReturn(saved);

        var dto = service.createReview(1L, reviewRequest());

        assertEquals(ReviewStatus.PENDING, dto.getStatus());
        verify(reviewRepository).save(any(TourReview.class));
    }

    // ─── updateReview ─────────────────────────────────────────────────────────

    @Test
    void updateReview_whenWrongUser_throwsForbidden() {
        User other = new User("other", "other@mail.com", "x");
        other.setId(99L);
        TourReview review = buildReview(other, tour, ReviewStatus.APPROVED);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateReview(user.getId(), 1L, new UpdateReviewRequest()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void updateReview_whenRejected_throwsBadRequest() {
        TourReview review = buildReview(user, tour, ReviewStatus.REJECTED);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateReview(user.getId(), 1L, new UpdateReviewRequest()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateReview_success_resetsToPending() {
        TourReview review = buildReview(user, tour, ReviewStatus.APPROVED);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any())).thenReturn(review);

        UpdateReviewRequest req = new UpdateReviewRequest();
        req.setComment("Updated comment text here");
        req.setRating(4);

        service.updateReview(user.getId(), 1L, req);

        assertEquals(ReviewStatus.PENDING, review.getStatus()); // back to moderation
        assertEquals(4, review.getRating());
    }

    // ─── deleteReview ─────────────────────────────────────────────────────────

    @Test
    void deleteReview_whenWrongUser_throwsForbidden() {
        User other = new User("other", "other@mail.com", "x");
        other.setId(99L);
        TourReview review = buildReview(other, tour, ReviewStatus.APPROVED);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteReview(user.getId(), 1L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void deleteReview_success_callsDelete() {
        TourReview review = buildReview(user, tour, ReviewStatus.APPROVED);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        service.deleteReview(user.getId(), 1L);

        verify(reviewRepository).delete(review);
    }

    // ─── Admin moderation ─────────────────────────────────────────────────────

    @Test
    void approveReview_setsStatusApproved() {
        TourReview review = buildReview(user, tour, ReviewStatus.PENDING);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any())).thenReturn(review);

        service.approveReview(1L);

        assertEquals(ReviewStatus.APPROVED, review.getStatus());
    }

    @Test
    void rejectReview_setsStatusRejected() {
        TourReview review = buildReview(user, tour, ReviewStatus.PENDING);
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any())).thenReturn(review);

        service.rejectReview(1L);

        assertEquals(ReviewStatus.REJECTED, review.getStatus());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private CreateReviewRequest reviewRequest() {
        CreateReviewRequest req = new CreateReviewRequest();
        req.setTourId(10L);
        req.setRating(5);
        req.setComment("Amazing experience, highly recommended!");
        return req;
    }

    private TourReview buildReview(User owner, Tour t, ReviewStatus status) {
        TourReview review = new TourReview();
        review.setUser(owner);
        review.setTour(t);
        review.setRating(5);
        review.setComment("Great tour!");
        review.setStatus(status);
        return review;
    }

    private Reservation buildReservation(Tour t, ReservationStatus status) {
        Reservation r = new Reservation();
        r.setTour(t);
        r.setReservationType(ReservationType.TOUR);
        r.setReservationStatus(status);
        return r;
    }
}
