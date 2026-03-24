package com.studyroom.booking.service;

import com.studyroom.booking.dto.AuthResponse;
import com.studyroom.booking.dto.LoginRequest;
import com.studyroom.booking.model.User;
import com.studyroom.booking.repository.UserRepository;
import com.studyroom.booking.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail() == null
                ? ""
                : loginRequest.getEmail().trim().toLowerCase();

        String password = loginRequest.getPassword() == null
                ? ""
                : loginRequest.getPassword();

        if (email.isBlank()) {
            throw new BadCredentialsException("Email is required");
        }

        if (password.isBlank()) {
            throw new BadCredentialsException("Password is required");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid credentials");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Authentication failed", e);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        if (user.getRole() == null) {
            throw new RuntimeException("User role is missing");
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponse(
                token,
                user.getId().toString(),
                user.getRole().name(),
                user.getEmail(),
                user.getName(),
                "Login successful"
        );
    }

    public String register(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new RuntimeException("Password is required");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            throw new RuntimeException("Full name is required");
        }

        if (user.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        String normalizedEmail = user.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        user.setEmail(normalizedEmail);
        user.setName(user.getName().trim());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        return "User registered successfully";
    }
}