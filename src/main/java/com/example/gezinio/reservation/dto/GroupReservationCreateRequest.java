package com.example.gezinio.reservation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class GroupReservationCreateRequest {

    @NotNull(message = "Tour ID is required")
    private Long tourId;

    @NotNull(message = "At least one reservation is required")
    @Size(min = 1)
    private List<ReservationCreateRequest> reservations;

    private LocalDate travelStartDate;
    private LocalDate travelEndDate;
    private String notes;

    public Long getTourId() { return tourId; }
    public void setTourId(Long tourId) { this.tourId = tourId; }

    public List<ReservationCreateRequest> getReservations() { return reservations; }
    public void setReservations(List<ReservationCreateRequest> reservations) { this.reservations = reservations; }

    public LocalDate getTravelStartDate() { return travelStartDate; }
    public void setTravelStartDate(LocalDate travelStartDate) { this.travelStartDate = travelStartDate; }

    public LocalDate getTravelEndDate() { return travelEndDate; }
    public void setTravelEndDate(LocalDate travelEndDate) { this.travelEndDate = travelEndDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}