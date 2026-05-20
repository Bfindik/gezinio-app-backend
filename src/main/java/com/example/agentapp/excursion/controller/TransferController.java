package com.example.agentapp.excursion.controller;

import com.example.agentapp.excursion.dto.TransferDTO;
import com.example.agentapp.excursion.dto.roomandtransfer.TransferCreateRequest;
import com.example.agentapp.excursion.dto.roomandtransfer.TransferUpdateRequest;
import com.example.agentapp.excursion.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfers", description = "Tour transfer (transport) management")
public class TransferController {

    private final TransferService transferService;

    @Autowired
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Create a new transfer for a tour")
    public ResponseEntity<TransferDTO> createTransfer(@Valid @RequestBody TransferCreateRequest request) {
        TransferDTO created = transferService.createTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a transfer by its ID")
    public ResponseEntity<TransferDTO> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(transferService.getTransferById(id));
    }

    @GetMapping("/tour/{tourId}")
    @Operation(summary = "List all transfers associated with a tour")
    public ResponseEntity<List<TransferDTO>> getTransfersByTour(@PathVariable Long tourId) {
        return ResponseEntity.ok(transferService.getTransfersByTour(tourId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Update an existing transfer")
    public ResponseEntity<TransferDTO> updateTransfer(
            @PathVariable Long id,
            @Valid @RequestBody TransferUpdateRequest request) {
        return ResponseEntity.ok(transferService.updateTransfer(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a transfer by its ID (ADMIN only)")
    public ResponseEntity<Void> deleteTransfer(@PathVariable Long id) {
        transferService.deleteTransfer(id);
        return ResponseEntity.noContent().build();
    }
}