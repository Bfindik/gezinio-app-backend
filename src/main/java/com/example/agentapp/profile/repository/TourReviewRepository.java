package com.example.agentapp.profile.repository;

import com.example.agentapp.profile.model.ReviewStatus;
import com.example.agentapp.profile.model.TourReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourReviewRepository extends JpaRepository<TourReview, Long> {

    List<TourReview> findByUserId(Long userId);

    List<TourReview> findByTourId(Long tourId);

    List<TourReview> findByTourIdAndStatus(Long tourId, ReviewStatus status);

    List<TourReview> findByStatus(ReviewStatus status);

    Optional<TourReview> findByUserIdAndTourId(Long userId, Long tourId);

    boolean existsByUserIdAndTourId(Long userId, Long tourId);

    long countByTourIdAndStatus(Long tourId, ReviewStatus status);

    @Query("SELECT AVG(r.rating) FROM TourReview r WHERE r.tour.id = :tourId AND r.status = 'APPROVED'")
    Double findAverageRatingByTourId(@Param("tourId") Long tourId);

    @Query("SELECT r.rating, COUNT(r) FROM TourReview r WHERE r.tour.id = :tourId AND r.status = 'APPROVED' GROUP BY r.rating")
    List<Object[]> findRatingDistributionByTourId(@Param("tourId") Long tourId);
}
