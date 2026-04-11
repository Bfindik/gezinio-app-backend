package com.example.agentapp.excursion.controller;

import com.example.agentapp.excursion.dto.HotelDTO;
import com.example.agentapp.excursion.dto.HotelCreateRequest;
import com.example.agentapp.excursion.dto.HotelUpdateRequest;
import com.example.agentapp.excursion.service.HotelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    @Autowired
    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<HotelDTO> createHotel(@Valid @RequestBody HotelCreateRequest request) {
        HotelDTO created = hotelService.createHotel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelDTO> getHotelById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @GetMapping
    public ResponseEntity<List<HotelDTO>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotels());
    }

    @GetMapping("/active")
    public ResponseEntity<List<HotelDTO>> getActiveHotels() {
        return ResponseEntity.ok(hotelService.getActiveHotels());
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<HotelDTO>> getHotelsByCity(@PathVariable String city) {
        return ResponseEntity.ok(hotelService.getHotelsByCity(city));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<HotelDTO> updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelUpdateRequest request) {
        return ResponseEntity.ok(hotelService.updateHotel(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
}