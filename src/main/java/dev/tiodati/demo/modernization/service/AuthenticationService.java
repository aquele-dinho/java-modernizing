package dev.tiodati.demo.modernization.service;

import dev.tiodati.demo.modernization.domain.User;
import dev.tiodati.demo.modernization.dto.JwtResponse;
import dev.tiodati.demo.modernization.dto.LoginRequest;
import dev.tiodati.demo.modernization.dto.RegisterRequest;
import dev.tiodati.demo.modernization.repository.UserRepository;
import dev.tiodati.demo.modernization.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling user authentication operations.
 * Manages user registration and login functionality.
 */
@Service
public class AuthenticationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    /**
     * Register a new user in the system.
     *
     * @param registerRequest registration details
     * @return JWT response with authentication token
     * @throws RuntimeException if username or email already exists
     */
    @Transactional
    public JwtResponse register(RegisterRequest registerRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRoles("USER"); // Default role
        
        userRepository.save(user);
        
        // Generate JWT token
        String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());
        
        return new JwtResponse(token, user.getUsername(), user.getEmail());
    }
    
    /**
     * Authenticate a user and generate JWT token.
     *
     * @param loginRequest login credentials
     * @return JWT response with authentication token
     */
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String token = jwtTokenProvider.generateToken(authentication);
        
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return new JwtResponse(token, user.getUsername(), user.getEmail());
    }
}
