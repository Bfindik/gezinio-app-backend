package com.example.agentapp.excursion.dto.tour_requests;

import com.example.agentapp.excursion.model.TourType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class TourCreateRequest {

    @NotBlank(message = "Tour name is required")
    @Size(min = 5, max = 200)
    private String name;

    @Size(max = 5000)
    private String description;

    @NotBlank(message = "Destination is required")
    @Size(min = 3, max = 200)
    private String destination;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal basePrice;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency = "TRY";

    @NotNull(message = "Duration is required")
    @Min(1)
    private Integer durationDays;

    @Min(1)
    private Integer maxCapacity;

    @NotNull(message = "Tour type is required")
    private TourType tourType;

    private List<String> includedServices;
    private List<String> excludedServices;
    private List<String> images;

    // İlişkiler (ID'ler)
    private List<Long> hotelIds;      // Otel ID'leri
    private List<Long> transferIds;   // Transfer ID'leri (veya yeni oluşturulacaklar)

    // Getters and Setters

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

    public List<Long> getHotelIds() {
        return hotelIds;
    }

    public void setHotelIds(List<Long> hotelIds) {
        this.hotelIds = hotelIds;
    }

    public List<Long> getTransferIds() {
        return transferIds;
    }

    public void setTransferIds(List<Long> transferIds) {
        this.transferIds = transferIds;
    }
}
