package com.example.gezinio.admin.dto;

import com.example.gezinio.reservation.model.ReservationStatus;
import com.example.gezinio.reservation.model.ReservationType;

import java.time.LocalDateTime;

public class ReservationFilterRequest {

    private ReservationStatus status;
    private ReservationType type;
    private Long userId;
    private Long tourId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public ReservationType getType() { return type; }
    public void setType(ReservationType type) { this.type = type; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTourId() { return tourId; }
    public void setTourId(Long tourId) { this.tourId = tourId; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
}
