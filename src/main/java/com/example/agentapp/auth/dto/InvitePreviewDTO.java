package com.example.agentapp.auth.dto;

public class InvitePreviewDTO {

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String type; // "activate" (only kind we mint today; reset password may add "reset" later)

    public InvitePreviewDTO() {}

    public InvitePreviewDTO(String username, String firstName, String lastName, String email, String type) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.type = type;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
