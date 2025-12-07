package dev.tiodati.demo.modernization.unit;

import dev.tiodati.demo.modernization.domain.Priority;
import dev.tiodati.demo.modernization.domain.Task;
import dev.tiodati.demo.modernization.domain.TaskStatus;
import dev.tiodati.demo.modernization.domain.User;
import dev.tiodati.demo.modernization.dto.TaskDto;
import dev.tiodati.demo.modernization.exception.ResourceNotFoundException;
import dev.tiodati.demo.modernization.repository.TaskRepository;
import dev.tiodati.demo.modernization.repository.UserRepository;
import dev.tiodati.demo.modernization.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService.
 * Tests use Mockito to isolate service logic from repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private TaskService taskService;
    
    private Task task;
    private User user;
    private TaskDto taskDto;
    
    @BeforeEach
    void setUp() {
        // Setup test user
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRoles("USER");
        user.setCreatedAt(LocalDateTime.now());
        
        // Setup test task
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.OPEN);
        task.setPriority(Priority.HIGH);
        task.setAssignedTo(user);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        // Setup test DTO
        taskDto = new TaskDto();
        taskDto.setTitle("New Task");
        taskDto.setDescription("New Description");
        taskDto.setStatus(TaskStatus.OPEN);
        taskDto.setPriority(Priority.MEDIUM);
        taskDto.setAssignedToId(1L);
    }
    
    @Test
    void testGetAllTasks() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Task> tasks = Arrays.asList(task);
        Page<Task> taskPage = new PageImpl<>(tasks, pageable, tasks.size());
        
        when(taskRepository.findAll(pageable)).thenReturn(taskPage);
        
        // Act
        Page<TaskDto> result = taskService.getAllTasks(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Task", result.getContent().get(0).getTitle());
        verify(taskRepository, times(1)).findAll(pageable);
    }
    
    @Test
    void testGetTaskById_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        
        // Act
        TaskDto result = taskService.getTaskById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Task", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(TaskStatus.OPEN, result.getStatus());
        assertEquals(Priority.HIGH, result.getPriority());
        assertEquals(1L, result.getAssignedToId());
        assertEquals("testuser", result.getAssignedToUsername());
        verify(taskRepository, times(1)).findById(1L);
    }
    
    @Test
    void testGetTaskById_NotFound() {
        // Arrange
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.getTaskById(999L);
        });
        verify(taskRepository, times(1)).findById(999L);
    }
    
    @Test
    void testGetTasksByUserId() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Task> tasks = Arrays.asList(task);
        Page<Task> taskPage = new PageImpl<>(tasks, pageable, tasks.size());
        
        when(taskRepository.findByAssignedToId(1L, pageable)).thenReturn(taskPage);
        
        // Act
        Page<TaskDto> result = taskService.getTasksByUserId(1L, pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getAssignedToId());
        verify(taskRepository, times(1)).findByAssignedToId(1L, pageable);
    }
    
    @Test
    void testCreateTask_WithAssignedUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        
        // Act
        TaskDto result = taskService.createTask(taskDto);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        assertEquals(1L, result.getAssignedToId());
        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    
    @Test
    void testCreateTask_WithoutAssignedUser() {
        // Arrange
        taskDto.setAssignedToId(null);
        
        Task unassignedTask = new Task();
        unassignedTask.setId(2L);
        unassignedTask.setTitle("New Task");
        unassignedTask.setDescription("New Description");
        unassignedTask.setStatus(TaskStatus.OPEN);
        unassignedTask.setPriority(Priority.MEDIUM);
        unassignedTask.setCreatedAt(LocalDateTime.now());
        unassignedTask.setUpdatedAt(LocalDateTime.now());
        
        when(taskRepository.save(any(Task.class))).thenReturn(unassignedTask);
        
        // Act
        TaskDto result = taskService.createTask(taskDto);
        
        // Assert
        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        assertNull(result.getAssignedToId());
        verify(userRepository, never()).findById(anyLong());
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    
    @Test
    void testCreateTask_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.createTask(taskDto);
        });
        verify(userRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    void testUpdateTask_Success() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        
        taskDto.setId(1L);
        taskDto.setTitle("Updated Task");
        
        // Act
        TaskDto result = taskService.updateTask(1L, taskDto);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    
    @Test
    void testUpdateTask_RemoveAssignedUser() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setId(1L);
            return savedTask;
        });
        
        taskDto.setId(1L);
        taskDto.setAssignedToId(null);
        
        // Act
        TaskDto result = taskService.updateTask(1L, taskDto);
        
        // Assert
        assertNotNull(result);
        verify(taskRepository, times(1)).findById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(taskRepository, times(1)).save(any(Task.class));
    }
    
    @Test
    void testUpdateTask_TaskNotFound() {
        // Arrange
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTask(999L, taskDto);
        });
        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    void testUpdateTask_UserNotFound() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        taskDto.setAssignedToId(999L);
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.updateTask(1L, taskDto);
        });
        verify(taskRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }
    
    @Test
    void testDeleteTask_Success() {
        // Arrange
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);
        
        // Act
        taskService.deleteTask(1L);
        
        // Assert
        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }
    
    @Test
    void testDeleteTask_NotFound() {
        // Arrange
        when(taskRepository.existsById(anyLong())).thenReturn(false);
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            taskService.deleteTask(999L);
        });
        verify(taskRepository, times(1)).existsById(999L);
        verify(taskRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testConvertToDto_WithAssignedUser() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        
        // Act
        TaskDto result = taskService.getTaskById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(task.getId(), result.getId());
        assertEquals(task.getTitle(), result.getTitle());
        assertEquals(task.getDescription(), result.getDescription());
        assertEquals(task.getStatus(), result.getStatus());
        assertEquals(task.getPriority(), result.getPriority());
        assertEquals(task.getAssignedTo().getId(), result.getAssignedToId());
        assertEquals(task.getAssignedTo().getUsername(), result.getAssignedToUsername());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }
    
    @Test
    void testConvertToDto_WithoutAssignedUser() {
        // Arrange
        Task unassignedTask = new Task();
        unassignedTask.setId(2L);
        unassignedTask.setTitle("Unassigned Task");
        unassignedTask.setDescription("No user");
        unassignedTask.setStatus(TaskStatus.OPEN);
        unassignedTask.setPriority(Priority.LOW);
        unassignedTask.setCreatedAt(LocalDateTime.now());
        unassignedTask.setUpdatedAt(LocalDateTime.now());
        
        when(taskRepository.findById(2L)).thenReturn(Optional.of(unassignedTask));
        
        // Act
        TaskDto result = taskService.getTaskById(2L);
        
        // Assert
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertNull(result.getAssignedToId());
        assertNull(result.getAssignedToUsername());
    }
}
