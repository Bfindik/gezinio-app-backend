package com.example.agentapp.admin.controller;

import com.example.agentapp.admin.dto.AdminTourStatsDTO;
import com.example.agentapp.admin.service.AdminTourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tours")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTourController {

    private final AdminTourService adminTourService;

    @Autowired
    public AdminTourController(AdminTourService adminTourService) {
        this.adminTourService = adminTourService;
    }

    @GetMapping("/stats")
    public ResponseEntity<List<AdminTourStatsDTO>> getTourStats() {
        return ResponseEntity.ok(adminTourService.getAllTourStats());
    }
}
