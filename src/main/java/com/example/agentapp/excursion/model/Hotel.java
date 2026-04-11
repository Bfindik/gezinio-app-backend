package com.example.agentapp.excursion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hotel name is required")
    @Size(min = 2, max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 2000)
    private String description;

    // Adres bilgileri
    @NotBlank(message = "City is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String country;

    @Column(length = 500)
    private String address;

    // Yıldız sayısı (1-5)
    @Column
    private Integer starRating;

    // Telefon
    @Column(length = 50)
    private String phone;

    // Email
    @Column(length = 100)
    private String email;

    // Website
    @Column(length = 200)
    private String website;

    // Fotoğraflar
    @ElementCollection
    @CollectionTable(name = "hotel_images",
            joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "image_url", length = 500)
    private List<String> images = new ArrayList<>();

    // Olanaklar (WiFi, Pool, Gym vs.)
    @ElementCollection
    @CollectionTable(name = "hotel_facilities",
            joinColumns = @JoinColumn(name = "hotel_id"))
    @Column(name = "facility", length = 100)
    private List<String> facilities = new ArrayList<>();

    // İlişkiler

    @ManyToMany(mappedBy = "hotels")
    private Set<Tour> tours = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Room> rooms = new ArrayList<>();

    // Aktif mi?
    @Column(nullable = false)
    private boolean active = true;

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

    public Hotel() {
    }

    public Hotel(String name, String city, String country) {
        this.name = name;
        this.city = city;
        this.country = country;
    }

    // Helper methods

    public void addRoom(Room room) {
        rooms.add(room);
        room.setHotel(this);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
        room.setHotel(null);
    }

    public String getFullLocation() {
        return city + ", " + country;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getStarRating() {
        return starRating;
    }

    public void setStarRating(Integer starRating) {
        this.starRating = starRating;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
    }

    public Set<Tour> getTours() {
        return tours;
    }

    public void setTours(Set<Tour> tours) {
        this.tours = tours;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hotel)) return false;
        Hotel hotel = (Hotel) o;
        return id != null && id.equals(hotel.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}