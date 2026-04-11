package com.example.agentapp.excursion.dto.roomandtransfer;

import com.example.agentapp.excursion.model.TransferType;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;


public class TransferUpdateRequest {

    private TransferType transferType;
    private String fromLocation;
    private String toLocation;
    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    private String currency;
    private Boolean includedInPrice;
    private String vehicleType;
    private Integer capacity;

    // Getters and Setters

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

    public Boolean getIncludedInPrice() {
        return includedInPrice;
    }

    public void setIncludedInPrice(Boolean includedInPrice) {
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
}
