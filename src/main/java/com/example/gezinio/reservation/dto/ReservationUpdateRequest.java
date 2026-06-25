package com.example.gezinio.reservation.dto;

import jakarta.validation.constraints.Min;

import java.time.LocalDate;

public class ReservationUpdateRequest {

    @Min(1)
    private Integer numberOfParticipants;

    private Long hotelId;
    private Long roomId;

    private String specialRequests;
    private LocalDate travelStartDate;
    private LocalDate travelEndDate;

    public Integer getNumberOfParticipants() { return numberOfParticipants; }
    public void setNumberOfParticipants(Integer numberOfParticipants) { this.numberOfParticipants = numberOfParticipants; }

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public LocalDate getTravelStartDate() { return travelStartDate; }
    public void setTravelStartDate(LocalDate travelStartDate) { this.travelStartDate = travelStartDate; }

    public LocalDate getTravelEndDate() { return travelEndDate; }
    public void setTravelEndDate(LocalDate travelEndDate) { this.travelEndDate = travelEndDate; }
}