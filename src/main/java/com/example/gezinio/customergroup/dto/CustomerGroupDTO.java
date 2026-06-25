package com.example.gezinio.customergroup.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CustomerGroupDTO {

    private Long id;
    private String groupCode;
    private String name;
    private String type;
    private String notes;

    private Long primaryContactId;
    private String primaryContactName;
    private String primaryContactEmail;

    private List<CustomerGroupMemberDTO> members;
    private int memberCount;

    // Combined aggregates across all members — names match frontend types.ts
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private BigDecimal combinedSpent;
    private String currency;

    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGroupCode() { return groupCode; }
    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Long getPrimaryContactId() { return primaryContactId; }
    public void setPrimaryContactId(Long primaryContactId) { this.primaryContactId = primaryContactId; }

    public String getPrimaryContactName() { return primaryContactName; }
    public void setPrimaryContactName(String primaryContactName) { this.primaryContactName = primaryContactName; }

    public String getPrimaryContactEmail() { return primaryContactEmail; }
    public void setPrimaryContactEmail(String primaryContactEmail) { this.primaryContactEmail = primaryContactEmail; }

    public List<CustomerGroupMemberDTO> getMembers() { return members; }
    public void setMembers(List<CustomerGroupMemberDTO> members) { this.members = members; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }

    public long getConfirmedBookings() { return confirmedBookings; }
    public void setConfirmedBookings(long confirmedBookings) { this.confirmedBookings = confirmedBookings; }

    public long getCancelledBookings() { return cancelledBookings; }
    public void setCancelledBookings(long cancelledBookings) { this.cancelledBookings = cancelledBookings; }

    public BigDecimal getCombinedSpent() { return combinedSpent; }
    public void setCombinedSpent(BigDecimal combinedSpent) { this.combinedSpent = combinedSpent; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
