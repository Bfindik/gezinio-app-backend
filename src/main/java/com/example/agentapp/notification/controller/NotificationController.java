package com.example.agentapp.notification.controller;

import com.example.agentapp.auth.security.UserPrincipal;
import com.example.agentapp.notification.model.NotificationLog;
import com.example.agentapp.notification.repository.NotificationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationLogRepository logRepository;

    @Autowired
    public NotificationController(NotificationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @GetMapping("/my")
    public ResponseEntity<List<NotificationLog>> getMyNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(logRepository.findByUserId(currentUser.getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationLog>> getAllNotifications() {
        return ResponseEntity.ok(logRepository.findAll());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<NotificationLog>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(logRepository.findByUserId(userId));
    }
}