package com.studyroom.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "New password is required")
    private String newPassword;

    // Default constructor
    public ForgotPasswordRequest() {
    }

    // Parameterized constructor
    public ForgotPasswordRequest(String email, String newPassword) {
        this.email = email;
        this.newPassword = newPassword;
    }

    // Getter for email
    public String getEmail() {
        return email;
    }

    // Setter for email
    public void setEmail(String email) {
        this.email = email;
    }

    // Getter for newPassword
    public String getNewPassword() {
        return newPassword;
    }

    // Setter for newPassword
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}