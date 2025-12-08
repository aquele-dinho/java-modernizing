package dev.tiodati.demo.modernization.controller;

import dev.tiodati.demo.modernization.dto.JwtResponse;
import dev.tiodati.demo.modernization.dto.LoginRequest;
import dev.tiodati.demo.modernization.dto.RegisterRequest;
import dev.tiodati.demo.modernization.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// MIGRATION NOTE: javax.validation.* will migrate to jakarta.validation.* in Spring Boot 3.x
import jakarta.validation.Valid;

/**
 * REST controller for authentication operations.
 * Handles user registration and login.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {
    
    @Autowired
    private AuthenticationService authenticationService;
    
    /**
     * Register a new user.
     *
     * @param registerRequest registration details
     * @return JWT token response
     */
    @Operation(summary = "Register new user", description = "Create a new user account and receive JWT token")
    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        JwtResponse response = authenticationService.register(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    /**
     * Authenticate user and generate JWT token.
     *
     * @param loginRequest login credentials
     * @return JWT token response
     */
    @Operation(summary = "Login", description = "Authenticate with username/password and receive JWT token")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse response = authenticationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
