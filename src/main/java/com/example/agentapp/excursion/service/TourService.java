package com.example.agentapp.excursion.service;

import com.example.agentapp.excursion.dto.HotelDTO;
import com.example.agentapp.excursion.dto.TransferDTO;
import com.example.agentapp.excursion.dto.TourDTO;
import com.example.agentapp.excursion.dto.tour_requests.TourCreateRequest;
import com.example.agentapp.excursion.dto.tour_requests.TourSearchRequest;
import com.example.agentapp.excursion.dto.tour_requests.TourUpdateRequest;
import com.example.agentapp.excursion.model.Hotel;
import com.example.agentapp.excursion.model.Tour;
import com.example.agentapp.excursion.model.TourStatus;
import com.example.agentapp.excursion.repository.HotelRepository;
import com.example.agentapp.excursion.repository.TourRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TourService {

    private static final Logger logger = LoggerFactory.getLogger(TourService.class);

    private final TourRepository tourRepository;
    private final HotelRepository hotelRepository;

    @Autowired
    public TourService(TourRepository tourRepository, HotelRepository hotelRepository) {
        this.tourRepository = tourRepository;
        this.hotelRepository = hotelRepository;
    }

    @Transactional
    public TourDTO createTour(TourCreateRequest request, Long createdBy) {
        if (tourRepository.existsByName(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tour with this name already exists");
        }

        Tour tour = new Tour(request.getName(), request.getDestination(), request.getBasePrice());
        tour.setDescription(request.getDescription());
        tour.setCurrency(request.getCurrency() != null ? request.getCurrency() : "TRY");
        tour.setCapacity(request.getMaxCapacity());
        tour.setDurationDays(request.getDurationDays());
        tour.setTourType(request.getTourType());
        tour.setTourStatus(TourStatus.DRAFT);
        tour.setCreatedBy(createdBy);

        if (request.getIncludedServices() != null) {
            tour.setIncludedServices(request.getIncludedServices());
        }
        if (request.getExcludedServices() != null) {
            tour.setExcludedServices(request.getExcludedServices());
        }
        if (request.getImages() != null) {
            tour.setImages(request.getImages());
        }

        if (request.getHotelIds() != null && !request.getHotelIds().isEmpty()) {
            Set<Hotel> hotels = new HashSet<>();
            for (Long hotelId : request.getHotelIds()) {
                Hotel hotel = hotelRepository.findById(hotelId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found: " + hotelId));
                hotels.add(hotel);
            }
            tour.setHotels(hotels);
        }

        Tour saved = tourRepository.save(tour);
        logger.info("Tour created with ID: {}", saved.getId());
        return toDTO(saved);
    }

    public TourDTO getTourById(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found: " + id));
        return toDTO(tour);
    }

    public List<TourDTO> getAllTours() {
        return tourRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TourDTO> getActiveTours() {
        return tourRepository.findByTourStatus(TourStatus.ACTIVE).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TourDTO> searchTours(TourSearchRequest request) {
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            return tourRepository.searchActiveTours(request.getKeyword()).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        if (request.getDestination() != null && request.getMinPrice() != null && request.getMaxPrice() != null) {
            return tourRepository.findByDestinationContainingIgnoreCaseAndPriceBetweenAndTourStatus(
                    request.getDestination(), request.getMinPrice(), request.getMaxPrice(), TourStatus.ACTIVE)
                    .stream().map(this::toDTO).collect(Collectors.toList());
        }
        if (request.getDestination() != null) {
            return tourRepository.findByDestinationContainingIgnoreCase(request.getDestination()).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        if (request.getTourType() != null) {
            return tourRepository.findByTourTypeAndTourStatus(request.getTourType(), TourStatus.ACTIVE).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        return getActiveTours();
    }

    @Transactional
    public TourDTO updateTour(Long id, TourUpdateRequest request) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found: " + id));

        if (request.getName() != null) {
            if (!request.getName().equals(tour.getName()) && tourRepository.existsByName(request.getName())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Tour with this name already exists");
            }
            tour.setName(request.getName());
        }
        if (request.getDescription() != null) tour.setDescription(request.getDescription());
        if (request.getDestination() != null) tour.setDestination(request.getDestination());
        if (request.getBasePrice() != null) tour.setPrice(request.getBasePrice());
        if (request.getCurrency() != null) tour.setCurrency(request.getCurrency());
        if (request.getDurationDays() != null) tour.setDurationDays(request.getDurationDays());
        if (request.getMaxCapacity() != null) tour.setCapacity(request.getMaxCapacity());
        if (request.getTourType() != null) tour.setTourType(request.getTourType());
        if (request.getStatus() != null) tour.setTourStatus(request.getStatus());
        if (request.getIncludedServices() != null) tour.setIncludedServices(request.getIncludedServices());
        if (request.getExcludedServices() != null) tour.setExcludedServices(request.getExcludedServices());
        if (request.getImages() != null) tour.setImages(request.getImages());

        if (request.getHotelIds() != null) {
            Set<Hotel> hotels = new HashSet<>();
            for (Long hotelId : request.getHotelIds()) {
                Hotel hotel = hotelRepository.findById(hotelId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found: " + hotelId));
                hotels.add(hotel);
            }
            tour.setHotels(hotels);
        }

        Tour saved = tourRepository.save(tour);
        logger.info("Tour updated: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public TourDTO publishTour(Long id) {
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found: " + id));
        if (tour.getTourStatus() != TourStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only DRAFT tours can be published");
        }
        tour.publish();
        Tour saved = tourRepository.save(tour);
        logger.info("Tour published: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public void deleteTour(Long id) {
        if (!tourRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found: " + id);
        }
        tourRepository.deleteById(id);
        logger.info("Tour deleted: {}", id);
    }

    // DTO mapping

    public TourDTO toDTO(Tour tour) {
        TourDTO dto = new TourDTO();
        dto.setId(tour.getId());
        dto.setName(tour.getName());
        dto.setDescription(tour.getDescription());
        dto.setDestination(tour.getDestination());
        dto.setBasePrice(tour.getPrice());
        dto.setCurrency(tour.getCurrency());
        dto.setDurationDays(tour.getDurationDays());
        dto.setMaxCapacity(tour.getCapacity());
        dto.setTourType(tour.getTourType());
        dto.setStatus(tour.getTourStatus());
        dto.setIncludedServices(tour.getIncludedServices());
        dto.setExcludedServices(tour.getExcludedServices());
        dto.setImages(tour.getImages());
        dto.setCreatedAt(tour.getCreatedAt());
        dto.setUpdatedAt(tour.getUpdatedAt());

        if (tour.getHotels() != null) {
            dto.setHotels(tour.getHotels().stream()
                    .map(this::hotelToSimpleDTO)
                    .collect(Collectors.toList()));
        }

        if (tour.getTransfers() != null) {
            dto.setTransfers(tour.getTransfers().stream()
                    .map(this::transferToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private HotelDTO hotelToSimpleDTO(Hotel hotel) {
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
        return dto;
    }

    private TransferDTO transferToDTO(com.example.agentapp.excursion.model.Transfer transfer) {
        TransferDTO dto = new TransferDTO();
        dto.setId(transfer.getId());
        dto.setTourId(transfer.getTour() != null ? transfer.getTour().getId() : null);
        dto.setTourName(transfer.getTour() != null ? transfer.getTour().getName() : null);
        dto.setTransferType(transfer.getTransferType());
        dto.setFromLocation(transfer.getFromLocation());
        dto.setToLocation(transfer.getToLocation());
        dto.setDescription(transfer.getDescription());
        dto.setPrice(transfer.getPrice());
        dto.setCurrency(transfer.getCurrency());
        dto.setIncludedInPrice(transfer.isIncludedInPrice());
        dto.setVehicleType(transfer.getVehicleType());
        dto.setCapacity(transfer.getCapacity());
        dto.setCreatedAt(transfer.getCreatedAt());
        dto.setUpdatedAt(transfer.getUpdatedAt());
        return dto;
    }
}