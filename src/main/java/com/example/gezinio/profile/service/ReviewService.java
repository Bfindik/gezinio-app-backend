package com.example.gezinio.profile.service;

import com.example.gezinio.auth.model.User;
import com.example.gezinio.auth.repository.UserRepository;
import com.example.gezinio.excursion.model.Tour;
import com.example.gezinio.excursion.repository.TourRepository;
import com.example.gezinio.profile.dto.CreateReviewRequest;
import com.example.gezinio.profile.dto.ReviewStatsDTO;
import com.example.gezinio.profile.dto.TourReviewDTO;
import com.example.gezinio.profile.dto.UpdateReviewRequest;
import com.example.gezinio.profile.model.ReviewStatus;
import com.example.gezinio.profile.model.TourReview;
import com.example.gezinio.profile.repository.TourReviewRepository;
import com.example.gezinio.reservation.model.ReservationStatus;
import com.example.gezinio.reservation.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final TourReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    public ReviewService(TourReviewRepository reviewRepository,
                          UserRepository userRepository,
                          TourRepository tourRepository,
                          ReservationRepository reservationRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
        this.reservationRepository = reservationRepository;
    }

    // ─── User actions ─────────────────────────────────────────────────────────

    @Transactional
    public TourReviewDTO createReview(Long userId, CreateReviewRequest request) {
        Tour tour = findTour(request.getTourId());
        User user = findUser(userId);

        // Must have a confirmed/completed reservation for this tour
        boolean hasEligibleReservation = reservationRepository
                .findByUserId(userId).stream()
                .anyMatch(r -> r.getTour() != null
                        && r.getTour().getId().equals(tour.getId())
                        && (r.getReservationStatus() == ReservationStatus.CONFIRMED
                            || r.getReservationStatus() == ReservationStatus.COMPLETED));

        if (!hasEligibleReservation) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only review tours you have a confirmed or completed reservation for");
        }

        if (reviewRepository.existsByUserIdAndTourId(userId, tour.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You have already reviewed this tour");
        }

        TourReview review = new TourReview();
        review.setUser(user);
        review.setTour(tour);
        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        review.setStatus(ReviewStatus.PENDING);

        return toDTO(reviewRepository.save(review));
    }

    public List<TourReviewDTO> getMyReviews(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TourReviewDTO updateReview(Long userId, Long reviewId, UpdateReviewRequest request) {
        TourReview review = findReview(reviewId);

        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit your own reviews");
        }
        if (review.getStatus() == ReviewStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejected reviews cannot be edited");
        }

        if (request.getRating() != null) review.setRating(request.getRating());
        if (request.getTitle() != null) review.setTitle(request.getTitle());
        if (request.getComment() != null) review.setComment(request.getComment());

        // Reset to pending after edit so it goes through moderation again
        review.setStatus(ReviewStatus.PENDING);

        return toDTO(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        TourReview review = findReview(reviewId);
        if (!review.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own reviews");
        }
        reviewRepository.delete(review);
    }

    // ─── Public ───────────────────────────────────────────────────────────────

    public List<TourReviewDTO> getApprovedReviewsByTour(Long tourId) {
        findTour(tourId);
        return reviewRepository.findByTourIdAndStatus(tourId, ReviewStatus.APPROVED).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ReviewStatsDTO getTourReviewStats(Long tourId) {
        Tour tour = findTour(tourId);

        ReviewStatsDTO stats = new ReviewStatsDTO();
        stats.setTourId(tourId);
        stats.setTourName(tour.getName());
        stats.setAverageRating(reviewRepository.findAverageRatingByTourId(tourId));
        stats.setTotalReviews(reviewRepository.countByTourIdAndStatus(tourId, ReviewStatus.APPROVED));

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0L);
        for (Object[] row : reviewRepository.findRatingDistributionByTourId(tourId)) {
            distribution.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        stats.setRatingDistribution(distribution);

        return stats;
    }

    // ─── Admin actions ────────────────────────────────────────────────────────

    public List<TourReviewDTO> getPendingReviews() {
        return reviewRepository.findByStatus(ReviewStatus.PENDING).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TourReviewDTO approveReview(Long reviewId) {
        TourReview review = findReview(reviewId);
        review.setStatus(ReviewStatus.APPROVED);
        return toDTO(reviewRepository.save(review));
    }

    @Transactional
    public TourReviewDTO rejectReview(Long reviewId) {
        TourReview review = findReview(reviewId);
        review.setStatus(ReviewStatus.REJECTED);
        return toDTO(reviewRepository.save(review));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private TourReview findReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found: " + id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Tour findTour(Long id) {
        return tourRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found: " + id));
    }

    private TourReviewDTO toDTO(TourReview r) {
        TourReviewDTO dto = new TourReviewDTO();
        dto.setId(r.getId());
        dto.setUserId(r.getUser().getId());
        dto.setUsername(r.getUser().getUsername());
        dto.setTourId(r.getTour().getId());
        dto.setTourName(r.getTour().getName());
        dto.setRating(r.getRating());
        dto.setTitle(r.getTitle());
        dto.setComment(r.getComment());
        dto.setStatus(r.getStatus());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());
        return dto;
    }
}
