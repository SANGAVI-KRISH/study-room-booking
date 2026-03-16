package com.studyroom.booking.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.studyroom.booking.dto.ChangePasswordRequest;
import com.studyroom.booking.dto.ForgotPasswordRequest;
import com.studyroom.booking.dto.LoginRequest;
import com.studyroom.booking.dto.RegisterRequest;
import com.studyroom.booking.model.Role;
import com.studyroom.booking.model.User;
import com.studyroom.booking.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register new user
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already exists";
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        user.setDepartment(request.getDepartment());
        user.setPhone(request.getPhone());

        userRepository.save(user);
        return "Registration successful";
    }

    // Login user
    public User login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return user;
            }
        }

        return null;
    }

    // Get user profile by email
    public User getProfile(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // Update profile
    public String updateProfile(String email, User updatedUser) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "User not found";
        }

        User user = userOpt.get();
        user.setName(updatedUser.getName());
        user.setDepartment(updatedUser.getDepartment());
        user.setPhone(updatedUser.getPhone());

        userRepository.save(user);
        return "Profile updated successfully";
    }

    // Change password
    public String changePassword(ChangePasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return "User not found";
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return "Old password is incorrect";
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Password changed successfully";
    }

    // Forgot / Reset password
    public String resetPassword(ForgotPasswordRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return "User not found";
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Password reset successful";
    }
}