package com.example.gezinio.customergroup.controller;

import com.example.gezinio.customergroup.dto.AddMemberRequest;
import com.example.gezinio.customergroup.dto.CreateCustomerGroupRequest;
import com.example.gezinio.customergroup.dto.CustomerGroupDTO;
import com.example.gezinio.customergroup.dto.UpdateCustomerGroupRequest;
import com.example.gezinio.customergroup.model.MemberRelationship;
import com.example.gezinio.customergroup.service.CustomerGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customer-groups")
@Tag(name = "Admin — Customer Groups",
        description = "Persistent customer groups (households / families / companies) evaluated as one for bookings and lifetime spend")
public class CustomerGroupController {

    private final CustomerGroupService service;

    @Autowired
    public CustomerGroupController(CustomerGroupService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER_GROUP_READ')")
    @Operation(summary = "List all customer groups with combined metrics")
    public ResponseEntity<List<CustomerGroupDTO>> list() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_GROUP_READ')")
    @Operation(summary = "Get one customer group with members and aggregates")
    public ResponseEntity<CustomerGroupDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CUSTOMER_GROUP_CREATE')")
    @Operation(summary = "Create a new customer group")
    public ResponseEntity<CustomerGroupDTO> create(@Valid @RequestBody CreateCustomerGroupRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_GROUP_UPDATE')")
    @Operation(summary = "Edit name, type, notes, or primary contact")
    public ResponseEntity<CustomerGroupDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerGroupRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_GROUP_DELETE')")
    @Operation(summary = "Disband a customer group (member accounts are kept)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAuthority('CUSTOMER_GROUP_MANAGE_MEMBERS')")
    @Operation(summary = "Add a member to the group")
    public ResponseEntity<CustomerGroupDTO> addMember(
            @PathVariable Long id,
            @Valid @RequestBody AddMemberRequest request) {
        return ResponseEntity.ok(service.addMember(id, request));
    }

    @PatchMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('CUSTOMER_GROUP_MANAGE_MEMBERS')")
    @Operation(summary = "Change a member's relationship (HEAD promotes them to primary contact)")
    public ResponseEntity<CustomerGroupDTO> setRelationship(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam MemberRelationship relationship) {
        return ResponseEntity.ok(service.setRelationship(id, userId, relationship));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @PreAuthorize("hasAuthority('CUSTOMER_GROUP_MANAGE_MEMBERS')")
    @Operation(summary = "Remove a member from the group")
    public ResponseEntity<CustomerGroupDTO> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId) {
        return ResponseEntity.ok(service.removeMember(id, userId));
    }
}
