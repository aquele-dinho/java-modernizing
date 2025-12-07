package dev.tiodati.demo.modernization.service;

import dev.tiodati.demo.modernization.domain.User;
import dev.tiodati.demo.modernization.dto.UserDto;
import dev.tiodati.demo.modernization.exception.ResourceNotFoundException;
import dev.tiodati.demo.modernization.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing user operations.
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all users.
     *
     * @return list of all users as DTOs
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get user by ID.
     *
     * @param id user ID
     * @return user DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return convertToDto(user);
    }
    
    /**
     * Get user by username.
     *
     * @param username the username
     * @return user DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return convertToDto(user);
    }
    
    /**
     * Update user profile.
     *
     * @param id user ID
     * @param userDto updated user data
     * @return updated user DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        user.setEmail(userDto.getEmail());
        // Note: Username and password updates would require additional validation
        
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }
    
    /**
     * Delete user by ID.
     *
     * @param id user ID
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
    
    /**
     * Convert User entity to UserDto.
     *
     * @param user the user entity
     * @return user DTO
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
