package com.studyroom.booking.model;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 100)
    private String department;

    @Column(length = 20)
    private String phone;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    // ================= CONSTRUCTORS =================

    public User() {}

    public User(String name, String email, String password, Role role, String department, String phone) {
        this.name = normalize(name);
        this.email = normalize(email);
        this.password = password;
        this.role = role;
        this.department = normalize(department);
        this.phone = normalize(phone);
        this.isActive = true;
    }

    // ================= HELPER METHODS =================

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isStudent() {
        return this.role == Role.STUDENT;
    }

    public boolean isStaff() {
        return this.role == Role.STAFF;
    }

    public boolean isActiveUser() {
        return Boolean.TRUE.equals(this.isActive);
    }

    // ================= GETTERS & SETTERS =================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = normalize(name);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = normalize(email);
    }

    public String getPassword() {
        return password;
    }

    // IMPORTANT: always store encoded password
    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = normalize(department);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = normalize(phone);
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active != null ? active : true;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ================= DEBUG =================

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", department='" + department + '\'' +
                ", phone='" + phone + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}