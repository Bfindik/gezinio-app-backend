package com.example.gezinio.excursion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "tours")
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tour name is required")
    @Column(unique = true, nullable = false, length = 200)
    private String name;

    @Column(unique = true, nullable = false, length = 5000)
    private String description;

    @NotBlank(message = "Destination is required")
    @Column(unique = true, nullable = false, length = 100)
    private String destination;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2) // 10 basamak ve virgülden sonra 2 sayı
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    @Column(nullable = false, length = 3)
    private String currency = "TRY";

    @Column
    @Min(value = 1, message = "Max capacity must be at least 1")
    private Integer capacity;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    @Column(nullable = false)
    private Integer durationDays;

    @NotNull(message = "Tour type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private TourType tourType;

    @NotNull(message = "Tour status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private TourStatus tourStatus;

    @ElementCollection
    @CollectionTable(name = "tour_included_services",
            joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "service", length = 200)
    private List<String> includedServices = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tour_excluded_services",
            joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "service", length = 200)
    private List<String> excludedServices = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tour_images",
            joinColumns = @JoinColumn(name = "tour_id"))
    @Column(name = "image_url", length = 500)
    private List<String> images = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "tour_hotels",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "hotel_id")
    )
    private Set<Hotel> hotels = new HashSet<>();

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transfer> transfers = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Tour() {
    }

    public Tour(String name, String destination, BigDecimal basePrice) {
        this.name = name;
        this.destination = destination;
        this.price = basePrice;
    }

    public boolean isActive() {
        return tourStatus == TourStatus.ACTIVE;
    }
    public void publish() {
        if (this.tourStatus == TourStatus.DRAFT) {
            this.tourStatus = TourStatus.ACTIVE;
        }
    }
    public Set<Hotel> getHotels() {
        return hotels;
    }

    public void setHotels(Set<Hotel> hotels) {
        this.hotels = hotels;
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }

    public void setTransfers(List<Transfer> transfers) {
        this.transfers = transfers;
    }

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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public TourType getTourType() {
        return tourType;
    }

    public void setTourType(TourType tourType) {
        this.tourType = tourType;
    }

    public TourStatus getTourStatus() {
        return tourStatus;
    }

    public void setTourStatus(TourStatus tourStatus) {
        this.tourStatus = tourStatus;
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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
