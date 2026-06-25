package com.example.gezinio.excursion.service;

import com.example.gezinio.excursion.dto.TourDTO;
import com.example.gezinio.excursion.dto.tour_requests.TourCreateRequest;
import com.example.gezinio.excursion.dto.tour_requests.TourUpdateRequest;
import com.example.gezinio.excursion.model.Tour;
import com.example.gezinio.excursion.model.TourStatus;
import com.example.gezinio.excursion.model.TourType;
import com.example.gezinio.excursion.repository.HotelRepository;
import com.example.gezinio.excursion.repository.TourRepository;
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
class TourServiceTest {

    @Mock TourRepository tourRepository;
    @Mock HotelRepository hotelRepository;

    @InjectMocks TourService service;

    private Tour draftTour;
    private Tour activeTour;

    @BeforeEach
    void setUp() {
        draftTour = new Tour("Cappadocia Tour", "Cappadocia", new BigDecimal("500.00"));
        draftTour.setId(1L);
        draftTour.setTourStatus(TourStatus.DRAFT);
        draftTour.setTourType(TourType.GROUP);
        draftTour.setDurationDays(3);
        draftTour.setCurrency("TRY");

        activeTour = new Tour("Istanbul Tour", "Istanbul", new BigDecimal("300.00"));
        activeTour.setId(2L);
        activeTour.setTourStatus(TourStatus.ACTIVE);
        activeTour.setTourType(TourType.PRIVATE);
        activeTour.setDurationDays(2);
        activeTour.setCurrency("TRY");
    }

    // ─── createTour ───────────────────────────────────────────────────────────

    @Test
    void createTour_whenDuplicateName_throwsConflict() {
        when(tourRepository.existsByName("Cappadocia Tour")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createTour(createRequest("Cappadocia Tour"), 1L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createTour_whenHotelNotFound_throwsNotFound() {
        when(tourRepository.existsByName("New Tour")).thenReturn(false);
        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        TourCreateRequest req = createRequest("New Tour");
        req.setHotelIds(List.of(99L));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createTour(req, 1L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void createTour_success_savedAsDraft() {
        when(tourRepository.existsByName("New Tour")).thenReturn(false);
        when(tourRepository.save(any())).thenReturn(draftTour);

        TourDTO dto = service.createTour(createRequest("New Tour"), 1L);

        assertEquals(TourStatus.DRAFT, dto.getStatus());
        verify(tourRepository).save(any(Tour.class));
    }

    // ─── getTourById ──────────────────────────────────────────────────────────

    @Test
    void getTourById_whenNotFound_throwsNotFound() {
        when(tourRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getTourById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getTourById_success_returnsDTO() {
        when(tourRepository.findById(1L)).thenReturn(Optional.of(draftTour));

        TourDTO dto = service.getTourById(1L);

        assertEquals("Cappadocia Tour", dto.getName());
    }

    // ─── publishTour ──────────────────────────────────────────────────────────

    @Test
    void publishTour_whenNotDraft_throwsBadRequest() {
        when(tourRepository.findById(2L)).thenReturn(Optional.of(activeTour));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.publishTour(2L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void publishTour_success_setsStatusActive() {
        when(tourRepository.findById(1L)).thenReturn(Optional.of(draftTour));
        when(tourRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TourDTO dto = service.publishTour(1L);

        assertEquals(TourStatus.ACTIVE, dto.getStatus());
        verify(tourRepository).save(draftTour);
    }

    // ─── updateTour ───────────────────────────────────────────────────────────

    @Test
    void updateTour_whenNotFound_throwsNotFound() {
        when(tourRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateTour(99L, new TourUpdateRequest()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateTour_whenDuplicateName_throwsConflict() {
        when(tourRepository.findById(1L)).thenReturn(Optional.of(draftTour));
        when(tourRepository.existsByName("Istanbul Tour")).thenReturn(true);

        TourUpdateRequest req = new TourUpdateRequest();
        req.setName("Istanbul Tour"); // different from current "Cappadocia Tour"

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateTour(1L, req));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void updateTour_success_updatesFields() {
        when(tourRepository.findById(1L)).thenReturn(Optional.of(draftTour));
        when(tourRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TourUpdateRequest req = new TourUpdateRequest();
        req.setDestination("Pamukkale");

        TourDTO dto = service.updateTour(1L, req);

        assertEquals("Pamukkale", dto.getDestination());
    }

    // ─── deleteTour ───────────────────────────────────────────────────────────

    @Test
    void deleteTour_whenNotFound_throwsNotFound() {
        when(tourRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteTour(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteTour_success_callsDeleteById() {
        when(tourRepository.existsById(1L)).thenReturn(true);

        service.deleteTour(1L);

        verify(tourRepository).deleteById(1L);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private TourCreateRequest createRequest(String name) {
        TourCreateRequest req = new TourCreateRequest();
        req.setName(name);
        req.setDestination("Cappadocia");
        req.setBasePrice(new BigDecimal("500.00"));
        req.setCurrency("TRY");
        req.setDurationDays(3);
        req.setTourType(TourType.GROUP);
        return req;
    }
}