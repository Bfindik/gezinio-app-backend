package com.example.agentapp.excursion.dto.roomandtransfer;

import com.example.agentapp.excursion.model.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RoomCreateRequest {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotNull(message = "Room type is required")
    private RoomType roomType;

    @NotBlank(message = "Room name is required")
    private String name;

    private String description;

    @NotNull(message = "Capacity is required")
    @Min(1)
    private Integer capacity;

    @Min(1)
    private Integer bedCount;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerNight;

    private String currency = "TRY";

    private Integer roomSize;

    // Getters and Setters

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getBedCount() {
        return bedCount;
    }

    public void setBedCount(Integer bedCount) {
        this.bedCount = bedCount;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getRoomSize() {
        return roomSize;
    }

    public void setRoomSize(Integer roomSize) {
        this.roomSize = roomSize;
    }
}
