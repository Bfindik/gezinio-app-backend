package com.example.agentapp.profile.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class UserProfileDTO {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String userType;
    private Set<String> roles;
    private boolean emailVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    // Summary stats
    private long totalReservations;
    private long confirmedReservations;
    private long cancelledReservations;
    private long totalFavorites;
    private long totalReviews;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public long getTotalReservations() { return totalReservations; }
    public void setTotalReservations(long totalReservations) { this.totalReservations = totalReservations; }

    public long getConfirmedReservations() { return confirmedReservations; }
    public void setConfirmedReservations(long confirmedReservations) { this.confirmedReservations = confirmedReservations; }

    public long getCancelledReservations() { return cancelledReservations; }
    public void setCancelledReservations(long cancelledReservations) { this.cancelledReservations = cancelledReservations; }

    public long getTotalFavorites() { return totalFavorites; }
    public void setTotalFavorites(long totalFavorites) { this.totalFavorites = totalFavorites; }

    public long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(long totalReviews) { this.totalReviews = totalReviews; }
}
