package com.example.gezinio.customergroup.dto;

import com.example.gezinio.customergroup.model.MemberRelationship;
import jakarta.validation.constraints.NotNull;

public class AddMemberRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "relationship is required")
    private MemberRelationship relationship;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public MemberRelationship getRelationship() { return relationship; }
    public void setRelationship(MemberRelationship relationship) { this.relationship = relationship; }
}
