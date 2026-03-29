package com.studyroom.booking.service;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.studyroom.booking.dto.ChangePasswordRequest;
import com.studyroom.booking.dto.ForgotPasswordRequest;
import com.studyroom.booking.dto.LoginRequest;
import com.studyroom.booking.dto.RegisterRequest;
import com.studyroom.booking.dto.UpdatePasswordRequest;
import com.studyroom.booking.model.Role;
import com.studyroom.booking.model.User;
import com.studyroom.booking.repository.UserRepository;

@Service
public class UserService {

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ================= REGISTER =================
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

    // ================= LOGIN =================
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

    // ================= GET PROFILE =================
    public User getProfile(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // ================= UPDATE PROFILE =================
    public String updateProfile(String email, User updatedUser) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "User not found";
        }

        User user = userOpt.get();

        // Only allow safe fields to update
        user.setName(updatedUser.getName());
        user.setDepartment(updatedUser.getDepartment());
        user.setPhone(updatedUser.getPhone());

        userRepository.save(user);
        return "Profile updated successfully";
    }

    // ================= UPDATE PASSWORD (PROFILE PAGE) =================
    public String updatePassword(String email, UpdatePasswordRequest request) {

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return "User not found";
        }

        User user = userOpt.get();

        // Validation
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            return "Current password is required";
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return "New password is required";
        }

        if (request.getConfirmPassword() == null || request.getConfirmPassword().isBlank()) {
            return "Confirm password is required";
        }

        // Check current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return "Current password is incorrect";
        }

        // Check new password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return "New password and confirm password do not match";
        }

        // Minimum length check
        if (request.getNewPassword().length() < 6) {
            return "New password must be at least 6 characters";
        }

        // Prevent same password reuse
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            return "New password cannot be same as current password";
        }

        // Save new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return "Password updated successfully";
    }

    // ================= CHANGE PASSWORD (OLD METHOD - OPTIONAL) =================
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

    // ================= RESET PASSWORD =================
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