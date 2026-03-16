package com.studyroom.booking.dto;

public class AuthResponse {

    private String token;
    private String userId;
    private String role;
    private String email;
    private String name;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String token,
                        String userId,
                        String role,
                        String email,
                        String name,
                        String message) {
        this.token = token;
        this.userId = userId;
        this.role = role;
        this.email = email;
        this.name = name;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}