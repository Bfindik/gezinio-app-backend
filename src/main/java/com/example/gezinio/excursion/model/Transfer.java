package com.example.gezinio.excursion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private Tour tour;

    @NotNull(message = "Transfer type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransferType transferType;

    @NotBlank(message = "From location is required")
    @Column(nullable = false, length = 200)
    private String fromLocation;  // Örn: "Sabiha Gökçen Havaalanı"

    @NotBlank(message = "To location is required")
    @Column(nullable = false, length = 200)
    private String toLocation;    // Örn: "Otel"

    @Column(length = 1000)
    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 3)
    private String currency = "TRY";

    @Column(nullable = false)
    private boolean includedInPrice = true;

    @Column(length = 100)
    private String vehicleType;

    @Column
    private Integer capacity;

    @Column(nullable = false)
    private  LocalDateTime transferDate;

    // Audit
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors

    public Transfer() {
    }

    public Transfer(Tour tour, TransferType transferType, String fromLocation, String toLocation, LocalDateTime transferDate) {
        this.tour = tour;
        this.transferType = transferType;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.transferDate = transferDate;
    }

// Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getTransferDate() {
        return transferDate;
    }

    public void setTransferDate(LocalDateTime transferDate) {
        this.transferDate = transferDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transfer)) return false;
        Transfer transfer = (Transfer) o;
        return id != null && id.equals(transfer.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}