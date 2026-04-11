package com.example.agentapp.reservation.dto;

import com.example.agentapp.reservation.model.PaymentStatus;
import com.example.agentapp.reservation.model.ReservationStatus;
import com.example.agentapp.reservation.model.ReservationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationDTO {

    private Long id;
    private Long userId;
    private String username;
    private ReservationType reservationType;

    // Tour bilgisi
    private Long tourId;
    private String tourName;
    private Long hotelId;
    private String hotelName;
    private Long roomId;
    private String roomName;

    // Transfer bilgisi
    private Long transferId;
    private String transferFromLocation;
    private String transferToLocation;

    // Grup bilgisi
    private Long groupReservationId;
    private String groupCode;

    private ReservationStatus reservationStatus;
    private PaymentStatus paymentStatus;

    private Integer numberOfParticipants;
    private BigDecimal totalPrice;
    private String currency;
    private String specialRequests;

    private LocalDate travelStartDate;
    private LocalDate travelEndDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReservationDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public ReservationType getReservationType() { return reservationType; }
    public void setReservationType(ReservationType reservationType) { this.reservationType = reservationType; }

    public Long getTourId() { return tourId; }
    public void setTourId(Long tourId) { this.tourId = tourId; }

    public String getTourName() { return tourName; }
    public void setTourName(String tourName) { this.tourName = tourName; }

    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public Long getTransferId() { return transferId; }
    public void setTransferId(Long transferId) { this.transferId = transferId; }

    public String getTransferFromLocation() { return transferFromLocation; }
    public void setTransferFromLocation(String transferFromLocation) { this.transferFromLocation = transferFromLocation; }

    public String getTransferToLocation() { return transferToLocation; }
    public void setTransferToLocation(String transferToLocation) { this.transferToLocation = transferToLocation; }

    public Long getGroupReservationId() { return groupReservationId; }
    public void setGroupReservationId(Long groupReservationId) { this.groupReservationId = groupReservationId; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public ReservationStatus getReservationStatus() { return reservationStatus; }
    public void setReservationStatus(ReservationStatus reservationStatus) { this.reservationStatus = reservationStatus; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public Integer getNumberOfParticipants() { return numberOfParticipants; }
    public void setNumberOfParticipants(Integer numberOfParticipants) { this.numberOfParticipants = numberOfParticipants; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public LocalDate getTravelStartDate() { return travelStartDate; }
    public void setTravelStartDate(LocalDate travelStartDate) { this.travelStartDate = travelStartDate; }

    public LocalDate getTravelEndDate() { return travelEndDate; }
    public void setTravelEndDate(LocalDate travelEndDate) { this.travelEndDate = travelEndDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}