package com.example.agentapp.profile.controller;

import com.example.agentapp.auth.security.UserPrincipal;
import com.example.agentapp.profile.dto.ChangePasswordRequest;
import com.example.agentapp.profile.dto.UpdateProfileRequest;
import com.example.agentapp.profile.dto.UserProfileDTO;
import com.example.agentapp.profile.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserProfileService profileService;

    @Autowired
    public UserProfileController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<UserProfileDTO> getProfile(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(profileService.getProfile(currentUser.getId()));
    }

    @PutMapping
    public ResponseEntity<UserProfileDTO> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(currentUser.getId(), request));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        profileService.changePassword(currentUser.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
