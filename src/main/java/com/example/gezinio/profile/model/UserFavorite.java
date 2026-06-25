package com.example.gezinio.profile.model;

import com.example.gezinio.auth.model.User;
import com.example.gezinio.excursion.model.Tour;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorites",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tour_id"}))
public class UserFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UserFavorite() {}

    public UserFavorite(User user, Tour tour) {
        this.user = user;
        this.tour = tour;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Tour getTour() { return tour; }
    public void setTour(Tour tour) { this.tour = tour; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
