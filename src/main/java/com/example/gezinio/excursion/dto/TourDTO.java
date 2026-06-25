package com.example.gezinio.excursion.dto;

import com.example.gezinio.excursion.model.TourStatus;
import com.example.gezinio.excursion.model.TourType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TourDTO {
    
    private Long id;
    private String name;
    private String description;
    private String destination;
    
    private BigDecimal basePrice;
    private String currency;
    
    private Integer durationDays;
    private Integer maxCapacity;
    
    private TourType tourType;
    private TourStatus status;
    
    private List<String> includedServices;
    private List<String> excludedServices;
    private List<String> images;
    
    // İlişkiler (circular olmadan!)
    private List<HotelDTO> hotels;       // Hotel detayları (içinde Tour YOK!)
    private List<TransferDTO> transfers; // Transfer detayları (içinde Tour YOK!)
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    
    // Constructors
    
    public TourDTO() {
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Integer getDurationDays() {
        return durationDays;
    }
    
    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }
    
    public Integer getMaxCapacity() {
        return maxCapacity;
    }
    
    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    
    public TourType getTourType() {
        return tourType;
    }
    
    public void setTourType(TourType tourType) {
        this.tourType = tourType;
    }
    
    public TourStatus getStatus() {
        return status;
    }
    
    public void setStatus(TourStatus status) {
        this.status = status;
    }
    
    public List<String> getIncludedServices() {
        return includedServices;
    }
    
    public void setIncludedServices(List<String> includedServices) {
        this.includedServices = includedServices;
    }
    
    public List<String> getExcludedServices() {
        return excludedServices;
    }
    
    public void setExcludedServices(List<String> excludedServices) {
        this.excludedServices = excludedServices;
    }
    
    public List<String> getImages() {
        return images;
    }
    
    public void setImages(List<String> images) {
        this.images = images;
    }
    
    public List<HotelDTO> getHotels() {
        return hotels;
    }
    
    public void setHotels(List<HotelDTO> hotels) {
        this.hotels = hotels;
    }
    
    public List<TransferDTO> getTransfers() {
        return transfers;
    }
    
    public void setTransfers(List<TransferDTO> transfers) {
        this.transfers = transfers;
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
