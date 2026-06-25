package com.example.gezinio.excursion.service;

import com.example.gezinio.excursion.dto.TransferDTO;
import com.example.gezinio.excursion.dto.roomandtransfer.TransferCreateRequest;
import com.example.gezinio.excursion.dto.roomandtransfer.TransferUpdateRequest;
import com.example.gezinio.excursion.model.Tour;
import com.example.gezinio.excursion.model.TourStatus;
import com.example.gezinio.excursion.model.TourType;
import com.example.gezinio.excursion.model.Transfer;
import com.example.gezinio.excursion.model.TransferType;
import com.example.gezinio.excursion.repository.TourRepository;
import com.example.gezinio.excursion.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock TransferRepository transferRepository;
    @Mock TourRepository tourRepository;

    @InjectMocks TransferService service;

    private Tour tour;
    private Transfer transfer;

    @BeforeEach
    void setUp() {
        tour = new Tour("Cappadocia Tour", "Cappadocia", new BigDecimal("500.00"));
        tour.setId(1L);
        tour.setTourStatus(TourStatus.ACTIVE);
        tour.setTourType(TourType.GROUP);
        tour.setDurationDays(3);

        transfer = new Transfer(tour, TransferType.AIRPORT_PICKUP,
                "Kayseri Airport", "Cappadocia Hotel", LocalDateTime.now().plusDays(7));
        transfer.setId(10L);
    }

    // ─── createTransfer ───────────────────────────────────────────────────────

    @Test
    void createTransfer_whenTourNotFound_throwsNotFound() {
        when(tourRepository.findById(99L)).thenReturn(Optional.empty());

        TransferCreateRequest req = createRequest(99L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createTransfer(req));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void createTransfer_success_savesAndReturnsDTO() {
        when(tourRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(transferRepository.save(any())).thenReturn(transfer);

        TransferDTO dto = service.createTransfer(createRequest(1L));

        assertNotNull(dto);
        assertEquals(TransferType.AIRPORT_PICKUP, dto.getTransferType());
        verify(transferRepository).save(any(Transfer.class));
    }

    // ─── getTransferById ──────────────────────────────────────────────────────

    @Test
    void getTransferById_whenNotFound_throwsNotFound() {
        when(transferRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getTransferById(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getTransferById_success_returnsDTO() {
        when(transferRepository.findById(10L)).thenReturn(Optional.of(transfer));

        TransferDTO dto = service.getTransferById(10L);

        assertEquals("Kayseri Airport", dto.getFromLocation());
        assertEquals("Cappadocia Hotel", dto.getToLocation());
    }

    // ─── getTransfersByTour ───────────────────────────────────────────────────

    @Test
    void getTransfersByTour_whenTourNotFound_throwsNotFound() {
        when(tourRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getTransfersByTour(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getTransfersByTour_success_returnsList() {
        when(tourRepository.existsById(1L)).thenReturn(true);
        when(transferRepository.findByTourId(1L)).thenReturn(List.of(transfer));

        List<TransferDTO> result = service.getTransfersByTour(1L);

        assertEquals(1, result.size());
    }

    // ─── updateTransfer ───────────────────────────────────────────────────────

    @Test
    void updateTransfer_whenNotFound_throwsNotFound() {
        when(transferRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateTransfer(99L, new TransferUpdateRequest()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateTransfer_success_updatesFields() {
        when(transferRepository.findById(10L)).thenReturn(Optional.of(transfer));
        when(transferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransferUpdateRequest req = new TransferUpdateRequest();
        req.setFromLocation("Istanbul Airport");

        TransferDTO dto = service.updateTransfer(10L, req);

        assertEquals("Istanbul Airport", dto.getFromLocation());
    }

    // ─── deleteTransfer ───────────────────────────────────────────────────────

    @Test
    void deleteTransfer_whenNotFound_throwsNotFound() {
        when(transferRepository.existsById(99L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteTransfer(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteTransfer_success_callsDeleteById() {
        when(transferRepository.existsById(10L)).thenReturn(true);

        service.deleteTransfer(10L);

        verify(transferRepository).deleteById(10L);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private TransferCreateRequest createRequest(Long tourId) {
        TransferCreateRequest req = new TransferCreateRequest();
        req.setTourId(tourId);
        req.setTransferType(TransferType.AIRPORT_PICKUP);
        req.setFromLocation("Kayseri Airport");
        req.setToLocation("Cappadocia Hotel");
        req.setTransferDate(LocalDateTime.now().plusDays(7));
        return req;
    }
}
