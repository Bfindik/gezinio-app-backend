package com.example.agentapp.reservation.service;

import com.example.agentapp.auth.model.User;
import com.example.agentapp.auth.repository.UserRepository;
import com.example.agentapp.excursion.model.*;
import com.example.agentapp.excursion.repository.HotelRepository;
import com.example.agentapp.excursion.repository.RoomRepository;
import com.example.agentapp.excursion.repository.TourRepository;
import com.example.agentapp.excursion.repository.TransferRepository;
import com.example.agentapp.reservation.dto.*;
import com.example.agentapp.reservation.model.*;
import com.example.agentapp.reservation.repository.GroupReservationRepository;
import com.example.agentapp.reservation.repository.ReservationRepository;
import com.example.agentapp.notification.event.AppNotificationEvent;
import com.example.agentapp.notification.model.NotificationEventType;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final GroupReservationRepository groupReservationRepository;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final TransferRepository transferRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository,
                              GroupReservationRepository groupReservationRepository,
                              UserRepository userRepository,
                              TourRepository tourRepository,
                              HotelRepository hotelRepository,
                              RoomRepository roomRepository,
                              TransferRepository transferRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.reservationRepository = reservationRepository;
        this.groupReservationRepository = groupReservationRepository;
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
        this.hotelRepository = hotelRepository;
        this.roomRepository = roomRepository;
        this.transferRepository = transferRepository;
        this.eventPublisher = eventPublisher;
    }

    // ─── Bireysel Rezervasyon ────────────────────────────────────────────────

    @Transactional
    public ReservationDTO createReservation(ReservationCreateRequest request, Long userId) {
        User user = findUser(userId);
        Reservation reservation = buildReservation(request, user);
        Reservation saved = reservationRepository.save(reservation);
        logger.info("Reservation created: {}", saved.getId());

        String tourName = saved.getTour() != null ? saved.getTour().getName() : null;
        String destination = saved.getTour() != null ? saved.getTour().getDestination() : null;
        eventPublisher.publishEvent(new AppNotificationEvent(
                this, NotificationEventType.RESERVATION_CREATED,
                user.getId(), user.getUsername(), user.getEmail(), user.getPhone()
        ).withReservation(saved.getId(), tourName, destination, saved.getTotalPrice(), saved.getCurrency()));

        return toDTO(saved);
    }

    public ReservationDTO getReservationById(Long id) {
        return toDTO(findReservation(id));
    }

    public List<ReservationDTO> getMyReservations(Long userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationDTO updateReservation(Long id, ReservationUpdateRequest request, Long userId) {
        Reservation reservation = findReservation(id);

        if (!reservation.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own reservations");
        }
        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED ||
                reservation.getReservationStatus() == ReservationStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update a " + reservation.getReservationStatus() + " reservation");
        }

        if (request.getNumberOfParticipants() != null) {
            reservation.setNumberOfParticipants(request.getNumberOfParticipants());
            // Fiyatı yeniden hesapla
            if (reservation.getReservationType() == ReservationType.TOUR && reservation.getTour() != null) {
                reservation.setTotalPrice(reservation.getTour().getPrice()
                        .multiply(BigDecimal.valueOf(request.getNumberOfParticipants())));
            } else if (reservation.getReservationType() == ReservationType.TRANSFER && reservation.getTransfer() != null
                    && reservation.getTransfer().getPrice() != null) {
                reservation.setTotalPrice(reservation.getTransfer().getPrice()
                        .multiply(BigDecimal.valueOf(request.getNumberOfParticipants())));
            }
        }
        if (request.getSpecialRequests() != null) reservation.setSpecialRequests(request.getSpecialRequests());
        if (request.getTravelStartDate() != null) reservation.setTravelStartDate(request.getTravelStartDate());
        if (request.getTravelEndDate() != null) reservation.setTravelEndDate(request.getTravelEndDate());

        if (request.getHotelId() != null) {
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));
            reservation.setHotel(hotel);
        }
        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
            reservation.setRoom(room);
        }

        return toDTO(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationDTO cancelReservation(Long id, Long userId) {
        Reservation reservation = findReservation(id);

        if (!reservation.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own reservations");
        }
        if (reservation.getReservationStatus() == ReservationStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation is already cancelled");
        }
        if (reservation.getReservationStatus() == ReservationStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completed reservations cannot be cancelled");
        }

        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);

        User user = saved.getUser();
        String tourName = saved.getTour() != null ? saved.getTour().getName() : null;
        String destination = saved.getTour() != null ? saved.getTour().getDestination() : null;
        eventPublisher.publishEvent(new AppNotificationEvent(
                this, NotificationEventType.RESERVATION_CANCELLED,
                user.getId(), user.getUsername(), user.getEmail(), user.getPhone()
        ).withReservation(saved.getId(), tourName, destination, saved.getTotalPrice(), saved.getCurrency()));

        return toDTO(saved);
    }

    @Transactional
    public ReservationDTO confirmReservation(Long id) {
        Reservation reservation = findReservation(id);
        if (reservation.getReservationStatus() != ReservationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING reservations can be confirmed");
        }
        reservation.setReservationStatus(ReservationStatus.CONFIRMED);
        Reservation saved = reservationRepository.save(reservation);

        User user = saved.getUser();
        String tourName = saved.getTour() != null ? saved.getTour().getName() : null;
        String destination = saved.getTour() != null ? saved.getTour().getDestination() : null;
        eventPublisher.publishEvent(new AppNotificationEvent(
                this, NotificationEventType.RESERVATION_CONFIRMED,
                user.getId(), user.getUsername(), user.getEmail(), user.getPhone()
        ).withReservation(saved.getId(), tourName, destination, saved.getTotalPrice(), saved.getCurrency()));

        return toDTO(saved);
    }

    // ─── Grup Rezervasyonu ───────────────────────────────────────────────────

    @Transactional
    public GroupReservationDTO createGroupReservation(GroupReservationCreateRequest request, Long userId) {
        Tour tour = findActiveTour(request.getTourId());

        GroupReservation group = new GroupReservation();
        group.setGroupCode(generateGroupCode());
        group.setTour(tour);
        group.setTravelStartDate(request.getTravelStartDate());
        group.setTravelEndDate(request.getTravelEndDate());
        group.setNotes(request.getNotes());

        // Her bir kişi için rezervasyon oluştur
        List<Reservation> reservations = new ArrayList<>();
        int totalParticipants = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (ReservationCreateRequest resReq : request.getReservations()) {
            User user = findUser(userId);
            Reservation reservation = buildReservation(resReq, user);
            reservation.setGroupReservation(group);
            reservations.add(reservation);
            totalParticipants += reservation.getNumberOfParticipants();
            totalPrice = totalPrice.add(reservation.getTotalPrice());
        }

        group.setReservations(reservations);
        group.setTotalParticipants(totalParticipants);
        group.setTotalPrice(totalPrice);
        group.setCurrency(tour.getCurrency());

        GroupReservation saved = groupReservationRepository.save(group);
        logger.info("Group reservation created: {} ({})", saved.getGroupCode(), saved.getId());
        return toGroupDTO(saved);
    }

    public GroupReservationDTO getGroupReservationById(Long id) {
        return toGroupDTO(findGroupReservation(id));
    }

    public GroupReservationDTO getGroupReservationByCode(String code) {
        GroupReservation group = groupReservationRepository.findByGroupCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found: " + code));
        return toGroupDTO(group);
    }

    public List<GroupReservationDTO> getAllGroupReservations() {
        return groupReservationRepository.findAll().stream()
                .map(this::toGroupDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public GroupReservationDTO cancelGroupReservation(Long id) {
        GroupReservation group = findGroupReservation(id);
        if (group.getStatus() == ReservationStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group reservation is already cancelled");
        }
        group.setStatus(ReservationStatus.CANCELLED);
        group.getReservations().forEach(r -> r.setReservationStatus(ReservationStatus.CANCELLED));
        return toGroupDTO(groupReservationRepository.save(group));
    }

    @Transactional
    public GroupReservationDTO confirmGroupReservation(Long id) {
        GroupReservation group = findGroupReservation(id);
        if (group.getStatus() != ReservationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PENDING group reservations can be confirmed");
        }
        group.setStatus(ReservationStatus.CONFIRMED);
        group.getReservations().forEach(r -> r.setReservationStatus(ReservationStatus.CONFIRMED));
        return toGroupDTO(groupReservationRepository.save(group));
    }

    // ─── Yardımcı metodlar ───────────────────────────────────────────────────

    private Reservation buildReservation(ReservationCreateRequest request, User user) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setReservationType(request.getReservationType());
        reservation.setNumberOfParticipants(request.getNumberOfParticipants());
        reservation.setSpecialRequests(request.getSpecialRequests());
        reservation.setTravelStartDate(request.getTravelStartDate());
        reservation.setTravelEndDate(request.getTravelEndDate());

        if (request.getReservationType() == ReservationType.TOUR) {
            if (request.getTourId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tour ID is required for TOUR reservation");
            }
            Tour tour = findActiveTour(request.getTourId());
            reservation.setTour(tour);
            reservation.setCurrency(tour.getCurrency());
            reservation.setTotalPrice(tour.getPrice().multiply(BigDecimal.valueOf(request.getNumberOfParticipants())));

            if (request.getHotelId() != null) {
                Hotel hotel = hotelRepository.findById(request.getHotelId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));
                reservation.setHotel(hotel);
            }
            if (request.getRoomId() != null) {
                Room room = roomRepository.findById(request.getRoomId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
                reservation.setRoom(room);
            }

        } else if (request.getReservationType() == ReservationType.TRANSFER) {
            if (request.getTransferId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer ID is required for TRANSFER reservation");
            }
            Transfer transfer = transferRepository.findById(request.getTransferId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found"));
            reservation.setTransfer(transfer);
            reservation.setCurrency(transfer.getCurrency());
            BigDecimal price = transfer.getPrice() != null ? transfer.getPrice() : BigDecimal.ZERO;
            reservation.setTotalPrice(price.multiply(BigDecimal.valueOf(request.getNumberOfParticipants())));
        }

        return reservation;
    }

    private String generateGroupCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String code = "GRP-" + timestamp;
        // Çakışma durumunda suffix ekle
        if (groupReservationRepository.existsByGroupCode(code)) {
            code = code + "-" + (int)(Math.random() * 1000);
        }
        return code;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Tour findActiveTour(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found: " + tourId));
        if (!tour.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tour is not active");
        }
        return tour;
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found: " + id));
    }

    private GroupReservation findGroupReservation(Long id) {
        return groupReservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group reservation not found: " + id));
    }

    // ─── DTO Mapping ─────────────────────────────────────────────────────────

    public ReservationDTO toDTO(Reservation r) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(r.getId());
        dto.setUserId(r.getUser().getId());
        dto.setUsername(r.getUser().getUsername());
        dto.setReservationType(r.getReservationType());
        dto.setReservationStatus(r.getReservationStatus());
        dto.setPaymentStatus(r.getPaymentStatus());
        dto.setNumberOfParticipants(r.getNumberOfParticipants());
        dto.setTotalPrice(r.getTotalPrice());
        dto.setCurrency(r.getCurrency());
        dto.setSpecialRequests(r.getSpecialRequests());
        dto.setTravelStartDate(r.getTravelStartDate());
        dto.setTravelEndDate(r.getTravelEndDate());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setUpdatedAt(r.getUpdatedAt());

        if (r.getTour() != null) {
            dto.setTourId(r.getTour().getId());
            dto.setTourName(r.getTour().getName());
        }
        if (r.getHotel() != null) {
            dto.setHotelId(r.getHotel().getId());
            dto.setHotelName(r.getHotel().getName());
        }
        if (r.getRoom() != null) {
            dto.setRoomId(r.getRoom().getId());
            dto.setRoomName(r.getRoom().getName());
        }
        if (r.getTransfer() != null) {
            dto.setTransferId(r.getTransfer().getId());
            dto.setTransferFromLocation(r.getTransfer().getFromLocation());
            dto.setTransferToLocation(r.getTransfer().getToLocation());
        }
        if (r.getGroupReservation() != null) {
            dto.setGroupReservationId(r.getGroupReservation().getId());
            dto.setGroupCode(r.getGroupReservation().getGroupCode());
        }

        return dto;
    }

    public GroupReservationDTO toGroupDTO(GroupReservation g) {
        GroupReservationDTO dto = new GroupReservationDTO();
        dto.setId(g.getId());
        dto.setGroupCode(g.getGroupCode());
        dto.setTourId(g.getTour().getId());
        dto.setTourName(g.getTour().getName());
        dto.setTotalParticipants(g.getTotalParticipants());
        dto.setTotalPrice(g.getTotalPrice());
        dto.setCurrency(g.getCurrency());
        dto.setStatus(g.getStatus());
        dto.setPaymentStatus(g.getPaymentStatus());
        dto.setTravelStartDate(g.getTravelStartDate());
        dto.setTravelEndDate(g.getTravelEndDate());
        dto.setNotes(g.getNotes());
        dto.setCreatedAt(g.getCreatedAt());
        dto.setUpdatedAt(g.getUpdatedAt());

        if (g.getReservations() != null) {
            dto.setReservations(g.getReservations().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}