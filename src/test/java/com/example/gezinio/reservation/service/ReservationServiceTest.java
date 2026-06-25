package com.example.gezinio.reservation.service;

import com.example.gezinio.auth.model.User;
import com.example.gezinio.auth.repository.UserRepository;
import com.example.gezinio.excursion.model.Tour;
import com.example.gezinio.excursion.model.TourStatus;
import com.example.gezinio.excursion.model.TourType;
import com.example.gezinio.excursion.repository.HotelRepository;
import com.example.gezinio.excursion.repository.RoomRepository;
import com.example.gezinio.excursion.repository.TourRepository;
import com.example.gezinio.excursion.repository.TransferRepository;
import com.example.gezinio.notification.event.AppNotificationEvent;
import com.example.gezinio.reservation.dto.ReservationCreateRequest;
import com.example.gezinio.reservation.dto.ReservationUpdateRequest;
import com.example.gezinio.reservation.model.*;
import com.example.gezinio.reservation.repository.GroupReservationRepository;
import com.example.gezinio.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock ReservationRepository reservationRepository;
    @Mock GroupReservationRepository groupReservationRepository;
    @Mock UserRepository userRepository;
    @Mock TourRepository tourRepository;
    @Mock HotelRepository hotelRepository;
    @Mock RoomRepository roomRepository;
    @Mock TransferRepository transferRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks ReservationService service;

    private User user;
    private Tour activeTour;
    private Tour inactiveTour;

    @BeforeEach
    void setUp() {
        user = new User("tourist", "tourist@mail.com", "hashed");
        user.setId(1L);

        activeTour = new Tour();
        activeTour.setId(10L);
        activeTour.setName("Cappadocia Tour");
        activeTour.setDestination("Cappadocia");
        activeTour.setPrice(new BigDecimal("500.00"));
        activeTour.setCurrency("TRY");
        activeTour.setTourStatus(TourStatus.ACTIVE);
        activeTour.setTourType(TourType.GROUP);
        activeTour.setDurationDays(3);

        inactiveTour = new Tour();
        inactiveTour.setId(11L);
        inactiveTour.setTourStatus(TourStatus.DRAFT);
    }

    @Test
    void createReservation_whenTourNotActive_throwsBadRequest() {
        ReservationCreateRequest req = tourRequest(inactiveTour.getId(), 2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tourRepository.findById(inactiveTour.getId())).thenReturn(Optional.of(inactiveTour));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createReservation(req, 1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createReservation_missingTourId_throwsBadRequest() {
        ReservationCreateRequest req = new ReservationCreateRequest();
        req.setReservationType(ReservationType.TOUR);
        req.setNumberOfParticipants(1);
        // tourId is null

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.createReservation(req, 1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createReservation_success_savesAndPublishesEvent() {
        ReservationCreateRequest req = tourRequest(activeTour.getId(), 2);
        Reservation saved = buildReservation(user, activeTour, ReservationStatus.PENDING);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tourRepository.findById(activeTour.getId())).thenReturn(Optional.of(activeTour));
        when(reservationRepository.save(any())).thenReturn(saved);

        service.createReservation(req, 1L);

        verify(reservationRepository).save(any(Reservation.class));
        verify(eventPublisher).publishEvent(any(AppNotificationEvent.class));
    }

    @Test
    void cancelReservation_whenWrongUser_throwsForbidden() {
        User otherUser = new User("other", "other@mail.com", "x");
        otherUser.setId(99L);
        Reservation reservation = buildReservation(otherUser, activeTour, ReservationStatus.PENDING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cancelReservation(1L, user.getId()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void cancelReservation_whenAlreadyCancelled_throwsBadRequest() {
        Reservation reservation = buildReservation(user, activeTour, ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cancelReservation(1L, user.getId()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void cancelReservation_whenCompleted_throwsBadRequest() {
        Reservation reservation = buildReservation(user, activeTour, ReservationStatus.COMPLETED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.cancelReservation(1L, user.getId()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void cancelReservation_success_setsStatusAndPublishesEvent() {
        Reservation reservation = buildReservation(user, activeTour, ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenReturn(reservation);

        service.cancelReservation(1L, user.getId());

        assertEquals(ReservationStatus.CANCELLED, reservation.getReservationStatus());
        verify(eventPublisher).publishEvent(any(AppNotificationEvent.class));
    }

    @Test
    void confirmReservation_whenNotPending_throwsBadRequest() {
        Reservation reservation = buildReservation(user, activeTour, ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.confirmReservation(1L));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void confirmReservation_success_setsStatusAndPublishesEvent() {
        Reservation reservation = buildReservation(user, activeTour, ReservationStatus.PENDING);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenReturn(reservation);

        service.confirmReservation(1L);

        assertEquals(ReservationStatus.CONFIRMED, reservation.getReservationStatus());
        verify(eventPublisher).publishEvent(any(AppNotificationEvent.class));
    }

    @Test
    void updateReservation_whenCancelled_throwsBadRequest() {
        Reservation reservation = buildReservation(user, activeTour, ReservationStatus.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateReservation(1L, new ReservationUpdateRequest(), user.getId()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateReservation_whenWrongUser_throwsForbidden() {
        User other = new User("x", "x@x.com", "x");
        other.setId(99L);
        Reservation reservation = buildReservation(other, activeTour, ReservationStatus.PENDING);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateReservation(1L, new ReservationUpdateRequest(), user.getId()));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private ReservationCreateRequest tourRequest(Long tourId, int participants) {
        ReservationCreateRequest req = new ReservationCreateRequest();
        req.setReservationType(ReservationType.TOUR);
        req.setTourId(tourId);
        req.setNumberOfParticipants(participants);
        return req;
    }

    private Reservation buildReservation(User owner, Tour tour, ReservationStatus status) {
        Reservation r = new Reservation();
        r.setUser(owner);
        r.setTour(tour);
        r.setReservationType(ReservationType.TOUR);
        r.setReservationStatus(status);
        r.setNumberOfParticipants(2);
        r.setTotalPrice(new BigDecimal("1000.00"));
        r.setCurrency("TRY");
        return r;
    }
}
