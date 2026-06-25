package com.example.gezinio.excursion.dto;

import com.example.gezinio.excursion.model.TransferType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferDTO {
    
    private Long id;
    
    private Long tourId;
    private String tourName;
    
    private TransferType transferType;
    
    private String fromLocation;
    private String toLocation;
    private String description;
    
    private BigDecimal price;
    private String currency;
    
    private boolean includedInPrice;
    
    private String vehicleType;
    private Integer capacity;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    
    public TransferDTO() {
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTourId() {
        return tourId;
    }
    
    public void setTourId(Long tourId) {
        this.tourId = tourId;
    }
    
    public String getTourName() {
        return tourName;
    }
    
    public void setTourName(String tourName) {
        this.tourName = tourName;
    }
    
    public TransferType getTransferType() {
        return transferType;
    }
    
    public void setTransferType(TransferType transferType) {
        this.transferType = transferType;
    }
    
    public String getFromLocation() {
        return fromLocation;
    }
    
    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }
    
    public String getToLocation() {
        return toLocation;
    }
    
    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public boolean isIncludedInPrice() {
        return includedInPrice;
    }
    
    public void setIncludedInPrice(boolean includedInPrice) {
        this.includedInPrice = includedInPrice;
    }
    
    public String getVehicleType() {
        return vehicleType;
    }
    
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
    
    public Integer getCapacity() {
        return capacity;
    }
    
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
