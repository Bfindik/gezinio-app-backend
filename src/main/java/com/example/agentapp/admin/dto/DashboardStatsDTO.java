package com.example.agentapp.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DashboardStatsDTO {

    // User metrics
    private long totalUsers;
    private long activeUsers;
    private long lockedUsers;
    private long newUsersLast30Days;

    // Reservation metrics
    private long totalReservations;
    private long pendingReservations;
    private long confirmedReservations;
    private long cancelledReservations;
    private long completedReservations;
    private long newReservationsLast30Days;

    // Revenue metrics (base currency)
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private BigDecimal totalRefunded;
    private BigDecimal netRevenue;

    // Tour metrics
    private long totalTours;
    private long activeTours;
    private long draftTours;
    private String mostBookedTourName;
    private long mostBookedTourCount;

    // Notification metrics
    private long totalEmailsSent;
    private long failedEmails;

    private LocalDateTime generatedAt;

    public DashboardStatsDTO() {
        this.generatedAt = LocalDateTime.now();
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public long getLockedUsers() { return lockedUsers; }
    public void setLockedUsers(long lockedUsers) { this.lockedUsers = lockedUsers; }

    public long getNewUsersLast30Days() { return newUsersLast30Days; }
    public void setNewUsersLast30Days(long newUsersLast30Days) { this.newUsersLast30Days = newUsersLast30Days; }

    public long getTotalReservations() { return totalReservations; }
    public void setTotalReservations(long totalReservations) { this.totalReservations = totalReservations; }

    public long getPendingReservations() { return pendingReservations; }
    public void setPendingReservations(long pendingReservations) { this.pendingReservations = pendingReservations; }

    public long getConfirmedReservations() { return confirmedReservations; }
    public void setConfirmedReservations(long confirmedReservations) { this.confirmedReservations = confirmedReservations; }

    public long getCancelledReservations() { return cancelledReservations; }
    public void setCancelledReservations(long cancelledReservations) { this.cancelledReservations = cancelledReservations; }

    public long getCompletedReservations() { return completedReservations; }
    public void setCompletedReservations(long completedReservations) { this.completedReservations = completedReservations; }

    public long getNewReservationsLast30Days() { return newReservationsLast30Days; }
    public void setNewReservationsLast30Days(long newReservationsLast30Days) { this.newReservationsLast30Days = newReservationsLast30Days; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getRevenueThisMonth() { return revenueThisMonth; }
    public void setRevenueThisMonth(BigDecimal revenueThisMonth) { this.revenueThisMonth = revenueThisMonth; }

    public BigDecimal getTotalRefunded() { return totalRefunded; }
    public void setTotalRefunded(BigDecimal totalRefunded) { this.totalRefunded = totalRefunded; }

    public BigDecimal getNetRevenue() { return netRevenue; }
    public void setNetRevenue(BigDecimal netRevenue) { this.netRevenue = netRevenue; }

    public long getTotalTours() { return totalTours; }
    public void setTotalTours(long totalTours) { this.totalTours = totalTours; }

    public long getActiveTours() { return activeTours; }
    public void setActiveTours(long activeTours) { this.activeTours = activeTours; }

    public long getDraftTours() { return draftTours; }
    public void setDraftTours(long draftTours) { this.draftTours = draftTours; }

    public String getMostBookedTourName() { return mostBookedTourName; }
    public void setMostBookedTourName(String mostBookedTourName) { this.mostBookedTourName = mostBookedTourName; }

    public long getMostBookedTourCount() { return mostBookedTourCount; }
    public void setMostBookedTourCount(long mostBookedTourCount) { this.mostBookedTourCount = mostBookedTourCount; }

    public long getTotalEmailsSent() { return totalEmailsSent; }
    public void setTotalEmailsSent(long totalEmailsSent) { this.totalEmailsSent = totalEmailsSent; }

    public long getFailedEmails() { return failedEmails; }
    public void setFailedEmails(long failedEmails) { this.failedEmails = failedEmails; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}