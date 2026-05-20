package com.example.agentapp.profile.controller;

import com.example.agentapp.auth.security.UserPrincipal;
import com.example.agentapp.profile.dto.CreateReviewRequest;
import com.example.agentapp.profile.dto.ReviewStatsDTO;
import com.example.agentapp.profile.dto.TourReviewDTO;
import com.example.agentapp.profile.dto.UpdateReviewRequest;
import com.example.agentapp.profile.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "User tour reviews and ratings, plus admin moderation endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ─── User endpoints ───────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new review for a tour")
    public ResponseEntity<TourReviewDTO> createReview(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(currentUser.getId(), request));
    }

    @GetMapping("/my")
    @Operation(summary = "List the current user's reviews")
    public ResponseEntity<List<TourReviewDTO>> getMyReviews(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(reviewService.getMyReviews(currentUser.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update one of the current user's reviews")
    public ResponseEntity<TourReviewDTO> updateReview(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(currentUser.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete one of the current user's reviews")
    public ResponseEntity<Void> deleteReview(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        reviewService.deleteReview(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    // ─── Public endpoints ─────────────────────────────────────────────────────

    @GetMapping("/tour/{tourId}")
    @Operation(summary = "List approved reviews for a tour")
    public ResponseEntity<List<TourReviewDTO>> getReviewsByTour(@PathVariable Long tourId) {
        return ResponseEntity.ok(reviewService.getApprovedReviewsByTour(tourId));
    }

    @GetMapping("/tour/{tourId}/stats")
    @Operation(summary = "Get aggregated review statistics for a tour")
    public ResponseEntity<ReviewStatsDTO> getTourReviewStats(@PathVariable Long tourId) {
        return ResponseEntity.ok(reviewService.getTourReviewStats(tourId));
    }

    // ─── Admin endpoints ──────────────────────────────────────────────────────

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List reviews awaiting moderation (ADMIN only)")
    public ResponseEntity<List<TourReviewDTO>> getPendingReviews() {
        return ResponseEntity.ok(reviewService.getPendingReviews());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a pending review (ADMIN only)")
    public ResponseEntity<TourReviewDTO> approveReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.approveReview(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a pending review (ADMIN only)")
    public ResponseEntity<TourReviewDTO> rejectReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.rejectReview(id));
    }
}
