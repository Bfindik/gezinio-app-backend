package com.example.agentapp.profile.controller;

import com.example.agentapp.auth.security.UserPrincipal;
import com.example.agentapp.profile.dto.FavoriteDTO;
import com.example.agentapp.profile.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Autowired
    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ResponseEntity<List<FavoriteDTO>> getMyFavorites(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(favoriteService.getMyFavorites(currentUser.getId()));
    }

    @PostMapping("/{tourId}")
    public ResponseEntity<FavoriteDTO> addFavorite(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long tourId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favoriteService.addFavorite(currentUser.getId(), tourId));
    }

    @DeleteMapping("/{tourId}")
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long tourId) {
        favoriteService.removeFavorite(currentUser.getId(), tourId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{tourId}/check")
    public ResponseEntity<Map<String, Boolean>> isFavorite(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long tourId) {
        boolean result = favoriteService.isFavorite(currentUser.getId(), tourId);
        return ResponseEntity.ok(Map.of("isFavorite", result));
    }
}
