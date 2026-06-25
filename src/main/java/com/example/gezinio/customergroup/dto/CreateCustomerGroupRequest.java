package com.example.gezinio.customergroup.dto;

import com.example.gezinio.customergroup.model.CustomerGroupType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateCustomerGroupRequest {

    @NotBlank(message = "name is required")
    @Size(max = 150)
    private String name;

    @NotNull(message = "type is required")
    private CustomerGroupType type;

    @Size(max = 1000)
    private String notes;

    @NotNull(message = "primaryContactId is required")
    private Long primaryContactId;

    @Valid
    private List<AddMemberRequest> members;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CustomerGroupType getType() { return type; }
    public void setType(CustomerGroupType type) { this.type = type; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getPrimaryContactId() { return primaryContactId; }
    public void setPrimaryContactId(Long primaryContactId) { this.primaryContactId = primaryContactId; }

    public List<AddMemberRequest> getMembers() { return members; }
    public void setMembers(List<AddMemberRequest> members) { this.members = members; }
}
