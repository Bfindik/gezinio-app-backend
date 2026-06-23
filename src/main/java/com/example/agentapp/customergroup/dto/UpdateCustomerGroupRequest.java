package com.example.agentapp.customergroup.dto;

import com.example.agentapp.customergroup.model.CustomerGroupType;
import jakarta.validation.constraints.Size;

public class UpdateCustomerGroupRequest {

    @Size(max = 150)
    private String name;

    private CustomerGroupType type;

    @Size(max = 1000)
    private String notes;

    private Long primaryContactId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CustomerGroupType getType() { return type; }
    public void setType(CustomerGroupType type) { this.type = type; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getPrimaryContactId() { return primaryContactId; }
    public void setPrimaryContactId(Long primaryContactId) { this.primaryContactId = primaryContactId; }
}
