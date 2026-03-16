package com.studyroom.booking.config;

import java.util.List;

import com.studyroom.booking.security.JwtAuthFilter;
import com.studyroom.booking.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            CustomUserDetailsService customUserDetailsService
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .authorizeHttpRequests(auth -> auth

                        // Allow preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public endpoints
                        .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()

                        // Admin module
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Staff module
                        .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")

                        // Student module
                        .requestMatchers("/api/student/**").hasAnyRole("STUDENT", "ADMIN")

                        // =========================
                        // ROOM MODULE
                        // =========================

                        // View rooms - all logged-in users
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**")
                        .hasAnyRole("ADMIN", "STAFF", "STUDENT")

                        // Manage rooms - admin only
                        .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasRole("ADMIN")

                        // =========================
                        // BOOKING MODULE
                        // =========================

                        // Create booking - all logged-in users
                        .requestMatchers(HttpMethod.POST, "/api/bookings/**")
                        .hasAnyRole("ADMIN", "STAFF", "STUDENT")

                        // View bookings - all logged-in users
                        .requestMatchers(HttpMethod.GET, "/api/bookings/**")
                        .hasAnyRole("ADMIN", "STAFF", "STUDENT")

                        // Approve / Reject booking - admin only
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/approve").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/*/reject").hasRole("ADMIN")

                        // Cancel / reschedule / update status - logged-in users
                        .requestMatchers(HttpMethod.PUT, "/api/bookings/**")
                        .hasAnyRole("ADMIN", "STAFF", "STUDENT")

                        // Delete booking - logged-in users
                        .requestMatchers(HttpMethod.DELETE, "/api/bookings/**")
                        .hasAnyRole("ADMIN", "STAFF", "STUDENT")

                        // Any other request must be authenticated
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Allow H2 console frame
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}