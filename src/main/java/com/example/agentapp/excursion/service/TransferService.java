package com.example.agentapp.excursion.service;

import com.example.agentapp.excursion.dto.TransferDTO;
import com.example.agentapp.excursion.dto.roomandtransfer.TransferCreateRequest;
import com.example.agentapp.excursion.dto.roomandtransfer.TransferUpdateRequest;
import com.example.agentapp.excursion.model.Tour;
import com.example.agentapp.excursion.model.Transfer;
import com.example.agentapp.excursion.repository.TourRepository;
import com.example.agentapp.excursion.repository.TransferRepository;
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
public class TransferService {

    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    private final TransferRepository transferRepository;
    private final TourRepository tourRepository;

    @Autowired
    public TransferService(TransferRepository transferRepository, TourRepository tourRepository) {
        this.transferRepository = transferRepository;
        this.tourRepository = tourRepository;
    }

    @Transactional
    public TransferDTO createTransfer(TransferCreateRequest request) {
        Tour tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found: " + request.getTourId()));

        Transfer transfer = new Transfer(tour, request.getTransferType(),
                request.getFromLocation(), request.getToLocation(), request.getTransferDate());
        transfer.setDescription(request.getDescription());
        transfer.setPrice(request.getPrice());
        transfer.setCurrency(request.getCurrency() != null ? request.getCurrency() : "TRY");
        if (request.getIncludedInPrice() != null) transfer.setIncludedInPrice(request.getIncludedInPrice());
        transfer.setVehicleType(request.getVehicleType());
        transfer.setCapacity(request.getCapacity());

        Transfer saved = transferRepository.save(transfer);
        logger.info("Transfer created with ID: {}", saved.getId());
        return toDTO(saved);
    }

    public TransferDTO getTransferById(Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found: " + id));
        return toDTO(transfer);
    }

    public List<TransferDTO> getTransfersByTour(Long tourId) {
        if (!tourRepository.existsById(tourId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tour not found: " + tourId);
        }
        return transferRepository.findByTourId(tourId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransferDTO updateTransfer(Long id, TransferUpdateRequest request) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found: " + id));

        if (request.getTransferType() != null) transfer.setTransferType(request.getTransferType());
        if (request.getFromLocation() != null) transfer.setFromLocation(request.getFromLocation());
        if (request.getToLocation() != null) transfer.setToLocation(request.getToLocation());
        if (request.getDescription() != null) transfer.setDescription(request.getDescription());
        if (request.getPrice() != null) transfer.setPrice(request.getPrice());
        if (request.getCurrency() != null) transfer.setCurrency(request.getCurrency());
        if (request.getIncludedInPrice() != null) transfer.setIncludedInPrice(request.getIncludedInPrice());
        if (request.getVehicleType() != null) transfer.setVehicleType(request.getVehicleType());
        if (request.getCapacity() != null) transfer.setCapacity(request.getCapacity());

        Transfer saved = transferRepository.save(transfer);
        logger.info("Transfer updated: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public void deleteTransfer(Long id) {
        if (!transferRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found: " + id);
        }
        transferRepository.deleteById(id);
        logger.info("Transfer deleted: {}", id);
    }

    public TransferDTO toDTO(Transfer transfer) {
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