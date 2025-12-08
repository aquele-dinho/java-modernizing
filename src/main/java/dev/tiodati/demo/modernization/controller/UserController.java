package dev.tiodati.demo.modernization.controller;

import dev.tiodati.demo.modernization.dto.UserDto;
import dev.tiodati.demo.modernization.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// MIGRATION NOTE: javax.validation.* will migrate to jakarta.validation.* in Spring Boot 3.x
import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for user management operations.
 * Most endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Get all users.
     * Requires ADMIN role.
     *
     * @return list of all users
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    /**
     * Get user by ID.
     *
     * @param id user ID
     * @return user DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    /**
     * Update user profile.
     *
     * @param id user ID
     * @param userDto updated user data
     * @return updated user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }
    
    /**
     * Delete user.
     * Requires ADMIN role.
     *
     * @param id user ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
