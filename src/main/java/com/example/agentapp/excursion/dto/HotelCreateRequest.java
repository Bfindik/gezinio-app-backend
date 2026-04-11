package com.example.agentapp.excursion.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class HotelCreateRequest {
    
    @NotBlank(message = "Hotel name is required")
    @Size(min = 2, max = 200)
    private String name;
    
    @Size(max = 2000)
    private String description;
    
    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;
    
    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;
    
    @Size(max = 500)
    private String address;
    
    @Min(1)
    @Max(5)
    private Integer starRating;
    
    @Size(max = 50)
    private String phone;
    
    @Size(max = 100)
    private String email;
    
    @Size(max = 200)
    private String website;
    
    private List<String> images;
    private List<String> facilities;
    
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
}



