package com.studyroom.booking.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.studyroom.booking.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find user by email (used for login and profile)
    Optional<User> findByEmail(String email);

    // Check if email already exists (used during registration)
    boolean existsByEmail(String email);
}