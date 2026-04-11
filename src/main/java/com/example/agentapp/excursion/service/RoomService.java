package com.example.agentapp.excursion.service;

import com.example.agentapp.excursion.dto.RoomDTO;
import com.example.agentapp.excursion.dto.roomandtransfer.RoomCreateRequest;
import com.example.agentapp.excursion.dto.roomandtransfer.RoomUpdateRequest;
import com.example.agentapp.excursion.model.Hotel;
import com.example.agentapp.excursion.model.Room;
import com.example.agentapp.excursion.repository.HotelRepository;
import com.example.agentapp.excursion.repository.RoomRepository;
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
public class RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @Autowired
    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    @Transactional
    public RoomDTO createRoom(RoomCreateRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found: " + request.getHotelId()));

        Room room = new Room(hotel, request.getRoomType(), request.getName(), request.getCapacity(), request.getPricePerNight());
        room.setDescription(request.getDescription());
        room.setBedCount(request.getBedCount());
        room.setCurrency(request.getCurrency() != null ? request.getCurrency() : "TRY");
        room.setRoomSize(request.getRoomSize());

        Room saved = roomRepository.save(room);
        logger.info("Room created with ID: {}", saved.getId());
        return toDTO(saved);
    }

    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found: " + id));
        return toDTO(room);
    }

    public List<RoomDTO> getRoomsByHotel(Long hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found: " + hotelId);
        }
        return roomRepository.findByHotelId(hotelId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDTO updateRoom(Long id, RoomUpdateRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found: " + id));

        if (request.getRoomType() != null) room.setRoomType(request.getRoomType());
        if (request.getName() != null) room.setName(request.getName());
        if (request.getDescription() != null) room.setDescription(request.getDescription());
        if (request.getCapacity() != null) room.setCapacity(request.getCapacity());
        if (request.getBedCount() != null) room.setBedCount(request.getBedCount());
        if (request.getPricePerNight() != null) room.setPricePerNight(request.getPricePerNight());
        if (request.getCurrency() != null) room.setCurrency(request.getCurrency());
        if (request.getRoomSize() != null) room.setRoomSize(request.getRoomSize());
        if (request.getAvailable() != null) room.setAvailable(request.getAvailable());

        Room saved = roomRepository.save(room);
        logger.info("Room updated: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found: " + id);
        }
        roomRepository.deleteById(id);
        logger.info("Room deleted: {}", id);
    }

    public RoomDTO toDTO(Room room) {
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