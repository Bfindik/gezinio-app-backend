package com.example.gezinio.profile.controller;

import com.example.gezinio.auth.security.UserPrincipal;
import com.example.gezinio.profile.dto.FavoriteDTO;
import com.example.gezinio.profile.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile/favorites")
@Tag(name = "Favorites", description = "Manage the current user's favorited tours")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Autowired
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    @Operation(summary = "List the current user's favorite tours")
    public ResponseEntity<List<FavoriteDTO>> getMyFavorites(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(favoriteService.getMyFavorites(currentUser.getId()));
    }

    @PostMapping("/{tourId}")
    @Operation(summary = "Add a tour to the current user's favorites")
    public ResponseEntity<FavoriteDTO> addFavorite(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long tourId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favoriteService.addFavorite(currentUser.getId(), tourId));
    }

    @DeleteMapping("/{tourId}")
    @Operation(summary = "Remove a tour from the current user's favorites")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long tourId) {
        favoriteService.removeFavorite(currentUser.getId(), tourId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tourId}/check")
    @Operation(summary = "Check whether a tour is in the current user's favorites")
    public ResponseEntity<Map<String, Boolean>> isFavorite(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long tourId) {
        boolean result = favoriteService.isFavorite(currentUser.getId(), tourId);
        return ResponseEntity.ok(Map.of("isFavorite", result));
    }
}
