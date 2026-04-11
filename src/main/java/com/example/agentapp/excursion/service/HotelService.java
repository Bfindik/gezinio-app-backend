package com.example.agentapp.excursion.service;

import com.example.agentapp.excursion.dto.HotelDTO;
import com.example.agentapp.excursion.dto.RoomDTO;
import com.example.agentapp.excursion.dto.HotelCreateRequest;
import com.example.agentapp.excursion.dto.HotelUpdateRequest;
import com.example.agentapp.excursion.model.Hotel;
import com.example.agentapp.excursion.model.Room;
import com.example.agentapp.excursion.repository.HotelRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HotelService {

    private static final Logger logger = LoggerFactory.getLogger(HotelService.class);

    private final HotelRepository hotelRepository;

    @Autowired
    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @Transactional
    public HotelDTO createHotel(HotelCreateRequest request) {
        if (hotelRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Hotel with this name already exists");
        }

        Hotel hotel = new Hotel(request.getName(), request.getCity(), request.getCountry());
        hotel.setDescription(request.getDescription());
        hotel.setAddress(request.getAddress());
        hotel.setStarRating(request.getStarRating());
        hotel.setPhone(request.getPhone());
        hotel.setEmail(request.getEmail());
        hotel.setWebsite(request.getWebsite());

        if (request.getImages() != null) hotel.setImages(request.getImages());
        if (request.getFacilities() != null) hotel.setFacilities(request.getFacilities());

        Hotel saved = hotelRepository.save(hotel);
        logger.info("Hotel created with ID: {}", saved.getId());
        return toDTO(saved);
    }

    public HotelDTO getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found: " + id));
        return toDTO(hotel);
    }

    public List<HotelDTO> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<HotelDTO> getActiveHotels() {
        return hotelRepository.findByActive(true).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<HotelDTO> getHotelsByCity(String city) {
        return hotelRepository.findByCityIgnoreCase(city).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public HotelDTO updateHotel(Long id, HotelUpdateRequest request) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(hotel.getName()) && hotelRepository.existsByName(request.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Hotel with this name already exists");
            }
            hotel.setName(request.getName());
        }
        if (request.getDescription() != null) hotel.setDescription(request.getDescription());
        if (request.getCity() != null) hotel.setCity(request.getCity());
        if (request.getCountry() != null) hotel.setCountry(request.getCountry());
        if (request.getAddress() != null) hotel.setAddress(request.getAddress());
        if (request.getStarRating() != null) hotel.setStarRating(request.getStarRating());
        if (request.getPhone() != null) hotel.setPhone(request.getPhone());
        if (request.getEmail() != null) hotel.setEmail(request.getEmail());
        if (request.getWebsite() != null) hotel.setWebsite(request.getWebsite());
        if (request.getImages() != null) hotel.setImages(request.getImages());
        if (request.getFacilities() != null) hotel.setFacilities(request.getFacilities());
        if (request.getActive() != null) hotel.setActive(request.getActive());

        Hotel saved = hotelRepository.save(hotel);
        logger.info("Hotel updated: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public void deleteHotel(Long id) {
        if (!hotelRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found: " + id);
        }
        hotelRepository.deleteById(id);
        logger.info("Hotel deleted: {}", id);
    }

    public HotelDTO toDTO(Hotel hotel) {
        HotelDTO dto = new HotelDTO();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setDescription(hotel.getDescription());
        dto.setCity(hotel.getCity());
        dto.setCountry(hotel.getCountry());
        dto.setAddress(hotel.getAddress());
        dto.setStarRating(hotel.getStarRating());
        dto.setPhone(hotel.getPhone());
        dto.setEmail(hotel.getEmail());
        dto.setWebsite(hotel.getWebsite());
        dto.setImages(hotel.getImages());
        dto.setFacilities(hotel.getFacilities());
        dto.setActive(hotel.isActive());
        dto.setCreatedAt(hotel.getCreatedAt());
        dto.setUpdatedAt(hotel.getUpdatedAt());

        if (hotel.getRooms() != null) {
            dto.setRooms(hotel.getRooms().stream()
                    .map(this::roomToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private RoomDTO roomToDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setHotelId(room.getHotel() != null ? room.getHotel().getId() : null);
        dto.setHotelName(room.getHotel() != null ? room.getHotel().getName() : null);
        dto.setRoomType(room.getRoomType());
        dto.setName(room.getName());
        dto.setDescription(room.getDescription());
        dto.setCapacity(room.getCapacity());
        dto.setBedCount(room.getBedCount());
        dto.setPricePerNight(room.getPricePerNight());
        dto.setCurrency(room.getCurrency());
        dto.setRoomSize(room.getRoomSize());
        dto.setAvailable(room.isAvailable());
        dto.setCreatedAt(room.getCreatedAt());
        dto.setUpdatedAt(room.getUpdatedAt());
        return dto;
    }
}