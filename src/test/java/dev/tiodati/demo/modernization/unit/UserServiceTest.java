package dev.tiodati.demo.modernization.unit;

import dev.tiodati.demo.modernization.domain.User;
import dev.tiodati.demo.modernization.dto.UserDto;
import dev.tiodati.demo.modernization.exception.ResourceNotFoundException;
import dev.tiodati.demo.modernization.repository.UserRepository;
import dev.tiodati.demo.modernization.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 * Tests use Mockito to isolate service logic from repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    private User user;
    private UserDto userDto;
    
    @BeforeEach
    void setUp() {
        // Setup test user
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword123");
        user.setRoles("USER");
        user.setCreatedAt(LocalDateTime.now());
        
        // Setup test DTO
        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setEmail("updated@example.com");
        userDto.setRoles("USER");
        userDto.setCreatedAt(LocalDateTime.now());
    }
    
    @Test
    void testGetAllUsers() {
        // Arrange
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("admin");
        user2.setEmail("admin@example.com");
        user2.setPassword("password");
        user2.setRoles("ADMIN");
        user2.setCreatedAt(LocalDateTime.now());
        
        List<User> users = Arrays.asList(user, user2);
        when(userRepository.findAll()).thenReturn(users);
        
        // Act
        List<UserDto> result = userService.getAllUsers();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testuser", result.getFirst().getUsername());
        assertEquals("admin", result.get(1).getUsername());
        verify(userRepository, times(1)).findAll();
    }
    
    @Test
    void testGetAllUsers_EmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList());
        
        // Act
        List<UserDto> result = userService.getAllUsers();
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository, times(1)).findAll();
    }
    
    @Test
    void testGetUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // Act
        UserDto result = userService.getUserById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("USER", result.getRoles());
        assertNotNull(result.getCreatedAt());
        verify(userRepository, times(1)).findById(1L);
    }
    
    @Test
    void testGetUserById_NotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999L);
        });
        verify(userRepository, times(1)).findById(999L);
    }
    
    @Test
    void testGetUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        
        // Act
        UserDto result = userService.getUserByUsername("testuser");
        
        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("USER", result.getRoles());
        verify(userRepository, times(1)).findByUsername("testuser");
    }
    
    @Test
    void testGetUserByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserByUsername("nonexistent");
        });
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }
    
    @Test
    void testUpdateUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User updatedUser = invocation.getArgument(0);
            updatedUser.setEmail("updated@example.com");
            return updatedUser;
        });
        
        userDto.setEmail("updated@example.com");
        
        // Act
        UserDto result = userService.updateUser(1L, userDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testUpdateUser_NotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(999L, userDto);
        });
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testDeleteUser_Success() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        
        // Act
        userService.deleteUser(1L);
        
        // Assert
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }
    
    @Test
    void testDeleteUser_NotFound() {
        // Arrange
        when(userRepository.existsById(anyLong())).thenReturn(false);
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(999L);
        });
        verify(userRepository, times(1)).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testConvertToDto_AllFieldsPopulated() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // Act
        UserDto result = userService.getUserById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getRoles(), result.getRoles());
        assertEquals(user.getCreatedAt(), result.getCreatedAt());
        
        // Verify password is NOT included in DTO (security requirement)
        // This is implicit since UserDto doesn't have a password field
    }
    
    @Test
    void testConvertToDto_AdminRole() {
        // Arrange
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setRoles("ADMIN");
        adminUser.setCreatedAt(LocalDateTime.now());
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(adminUser));
        
        // Act
        UserDto result = userService.getUserById(2L);
        
        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("admin", result.getUsername());
        assertEquals("ADMIN", result.getRoles());
    }
    
    @Test
    void testConvertToDto_MultipleRoles() {
        // Arrange
        User multiRoleUser = new User();
        multiRoleUser.setId(3L);
        multiRoleUser.setUsername("superuser");
        multiRoleUser.setEmail("super@example.com");
        multiRoleUser.setPassword("password");
        multiRoleUser.setRoles("USER,ADMIN");
        multiRoleUser.setCreatedAt(LocalDateTime.now());
        
        when(userRepository.findById(3L)).thenReturn(Optional.of(multiRoleUser));
        
        // Act
        UserDto result = userService.getUserById(3L);
        
        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("superuser", result.getUsername());
        assertEquals("USER,ADMIN", result.getRoles());
    }
    
    @Test
    void testGetAllUsers_VerifyPasswordNotExposed() {
        // Arrange
        List<User> users = Arrays.asList(user);
        when(userRepository.findAll()).thenReturn(users);
        
        // Act
        List<UserDto> result = userService.getAllUsers();
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        // Verify that the DTO doesn't expose password
        // This is implicit - UserDto doesn't have a password field
        UserDto dto = result.getFirst();
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
    }
}
