package com.example.agentapp.excursion.service;

import com.example.agentapp.excursion.dto.HotelCreateRequest;
import com.example.agentapp.excursion.dto.HotelDTO;
import com.example.agentapp.excursion.dto.HotelUpdateRequest;
import com.example.agentapp.excursion.model.Hotel;
import com.example.agentapp.excursion.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock HotelRepository hotelRepository;

    @InjectMocks HotelService service;

    private Hotel hotel;

    @BeforeEach
    void setUp() {
        hotel = new Hotel("Grand Hotel", "Istanbul", "Turkey");
        hotel.setId(1L);
        hotel.setStarRating(5);
    }

    // ─── createHotel ──────────────────────────────────────────────────────────

    @Test
    void createHotel_whenDuplicateName_throwsConflict() {
        when(hotelRepository.existsByName("Grand Hotel")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createHotel(createRequest("Grand Hotel")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createHotel_success_savesAndReturnsDTO() {
        when(hotelRepository.existsByName("New Hotel")).thenReturn(false);
        when(hotelRepository.save(any())).thenReturn(hotel);

        HotelDTO dto = service.createHotel(createRequest("New Hotel"));

        assertNotNull(dto);
        verify(hotelRepository).save(any(Hotel.class));
    }

    // ─── getHotelById ─────────────────────────────────────────────────────────

    @Test
    void getHotelById_whenNotFound_throwsNotFound() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getHotelById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getHotelById_success_returnsDTO() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        HotelDTO dto = service.getHotelById(1L);

        assertEquals("Grand Hotel", dto.getName());
        assertEquals("Istanbul", dto.getCity());
    }

    // ─── getActiveHotels / getHotelsByCity ────────────────────────────────────

    @Test
    void getActiveHotels_returnsOnlyActiveHotels() {
        when(hotelRepository.findByActive(true)).thenReturn(List.of(hotel));

        List<HotelDTO> result = service.getActiveHotels();

        assertEquals(1, result.size());
    }

    @Test
    void getHotelsByCity_returnsHotelsForCity() {
        when(hotelRepository.findByCityIgnoreCase("Istanbul")).thenReturn(List.of(hotel));

        List<HotelDTO> result = service.getHotelsByCity("Istanbul");

        assertEquals(1, result.size());
        assertEquals("Istanbul", result.get(0).getCity());
    }

    // ─── updateHotel ──────────────────────────────────────────────────────────

    @Test
    void updateHotel_whenNotFound_throwsNotFound() {
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateHotel(99L, new HotelUpdateRequest()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateHotel_whenDuplicateName_throwsConflict() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.existsByName("Other Hotel")).thenReturn(true);

        HotelUpdateRequest req = new HotelUpdateRequest();
        req.setName("Other Hotel");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateHotel(1L, req));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void updateHotel_success_updatesFields() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HotelUpdateRequest req = new HotelUpdateRequest();
        req.setCity("Ankara");

        HotelDTO dto = service.updateHotel(1L, req);

        assertEquals("Ankara", dto.getCity());
    }

    @Test
    void updateHotel_sameName_doesNotCheckDuplicate() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HotelUpdateRequest req = new HotelUpdateRequest();
        req.setName("Grand Hotel"); // same as existing

        assertDoesNotThrow(() -> service.updateHotel(1L, req));
        verify(hotelRepository, never()).existsByName("Grand Hotel");
    }

    // ─── deleteHotel ──────────────────────────────────────────────────────────

    @Test
    void deleteHotel_whenNotFound_throwsNotFound() {
        when(hotelRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteHotel(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteHotel_success_callsDeleteById() {
        when(hotelRepository.existsById(1L)).thenReturn(true);

        service.deleteHotel(1L);

        verify(hotelRepository).deleteById(1L);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private HotelCreateRequest createRequest(String name) {
        HotelCreateRequest req = new HotelCreateRequest();
        req.setName(name);
        req.setCity("Istanbul");
        req.setCountry("Turkey");
        req.setStarRating(5);
        return req;
    }
}