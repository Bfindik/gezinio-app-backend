package com.example.agentapp.excursion.service;

import com.example.agentapp.excursion.dto.RoomDTO;
import com.example.agentapp.excursion.dto.roomandtransfer.RoomCreateRequest;
import com.example.agentapp.excursion.dto.roomandtransfer.RoomUpdateRequest;
import com.example.agentapp.excursion.model.Hotel;
import com.example.agentapp.excursion.model.Room;
import com.example.agentapp.excursion.model.RoomType;
import com.example.agentapp.excursion.repository.HotelRepository;
import com.example.agentapp.excursion.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock RoomRepository roomRepository;
    @Mock HotelRepository hotelRepository;

    @InjectMocks RoomService service;

    private Hotel hotel;
    private Room room;

    @BeforeEach
    void setUp() {
        hotel = new Hotel("Grand Hotel", "Istanbul", "Turkey");
        hotel.setId(1L);

        room = new Room(hotel, RoomType.DOUBLE, "Deluxe Double", 2, new BigDecimal("250.00"));
        room.setId(10L);
    }

    // ─── createRoom ───────────────────────────────────────────────────────────

    @Test
    void createRoom_whenHotelNotFound_throwsNotFound() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        RoomCreateRequest req = createRequest(99L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createRoom(req));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void createRoom_success_savesAndReturnsDTO() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomRepository.save(any())).thenReturn(room);

        RoomDTO dto = service.createRoom(createRequest(1L));

        assertNotNull(dto);
        assertEquals(RoomType.DOUBLE, dto.getRoomType());
        verify(roomRepository).save(any(Room.class));
    }

    // ─── getRoomById ──────────────────────────────────────────────────────────

    @Test
    void getRoomById_whenNotFound_throwsNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getRoomById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getRoomById_success_returnsDTO() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));

        RoomDTO dto = service.getRoomById(10L);

        assertEquals("Deluxe Double", dto.getName());
    }

    // ─── getRoomsByHotel ──────────────────────────────────────────────────────

    @Test
    void getRoomsByHotel_whenHotelNotFound_throwsNotFound() {
        when(hotelRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getRoomsByHotel(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getRoomsByHotel_success_returnsList() {
        when(hotelRepository.existsById(1L)).thenReturn(true);
        when(roomRepository.findByHotelId(1L)).thenReturn(List.of(room));

        List<RoomDTO> result = service.getRoomsByHotel(1L);

        assertEquals(1, result.size());
    }

    // ─── updateRoom ───────────────────────────────────────────────────────────

    @Test
    void updateRoom_whenNotFound_throwsNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateRoom(99L, new RoomUpdateRequest()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateRoom_success_updatesFields() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(roomRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RoomUpdateRequest req = new RoomUpdateRequest();
        req.setAvailable(false);

        RoomDTO dto = service.updateRoom(10L, req);

        assertFalse(dto.isAvailable());
    }

    // ─── deleteRoom ───────────────────────────────────────────────────────────

    @Test
    void deleteRoom_whenNotFound_throwsNotFound() {
        when(roomRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteRoom(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteRoom_success_callsDeleteById() {
        when(roomRepository.existsById(10L)).thenReturn(true);

        service.deleteRoom(10L);

        verify(roomRepository).deleteById(10L);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private RoomCreateRequest createRequest(Long hotelId) {
        RoomCreateRequest req = new RoomCreateRequest();
        req.setHotelId(hotelId);
        req.setRoomType(RoomType.DOUBLE);
        req.setName("Deluxe Double");
        req.setCapacity(2);
        req.setPricePerNight(new BigDecimal("250.00"));
        return req;
    }
}