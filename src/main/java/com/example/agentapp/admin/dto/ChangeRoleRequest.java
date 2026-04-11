package com.example.agentapp.admin.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class ChangeRoleRequest {

    @NotEmpty(message = "At least one role must be specified")
    private Set<String> roles;

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}
