package com.studyroom.booking.service;

import java.util.Optional;

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
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not registered");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid credentials");
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid credentials");
        }

        User user = userOptional.get();

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

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setName(user.getName().trim());
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        return "User registered successfully";
    }
}