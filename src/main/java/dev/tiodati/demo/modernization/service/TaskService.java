package dev.tiodati.demo.modernization.service;

import dev.tiodati.demo.modernization.domain.Task;
import dev.tiodati.demo.modernization.domain.User;
import dev.tiodati.demo.modernization.dto.TaskDto;
import dev.tiodati.demo.modernization.exception.ResourceNotFoundException;
import dev.tiodati.demo.modernization.repository.TaskRepository;
import dev.tiodati.demo.modernization.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing task operations.
 */
@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all tasks with pagination.
     *
     * @param pageable pagination information
     * @return page of task DTOs
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(this::convertToDto);
    }
    
    /**
     * Get task by ID.
     *
     * @param id task ID
     * @return task DTO
     * @throws ResourceNotFoundException if task not found
     */
    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return convertToDto(task);
    }
    
    /**
     * Get tasks assigned to a specific user.
     *
     * @param userId user ID
     * @param pageable pagination information
     * @return page of task DTOs
     */
    @Transactional(readOnly = true)
    public Page<TaskDto> getTasksByUserId(Long userId, Pageable pageable) {
        return taskRepository.findByAssignedToId(userId, pageable)
                .map(this::convertToDto);
    }
    
    /**
     * Create a new task.
     *
     * @param taskDto task data
     * @return created task DTO
     */
    @Transactional
    public TaskDto createTask(TaskDto taskDto) {
        Task task = new Task();
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setStatus(taskDto.getStatus());
        task.setPriority(taskDto.getPriority());
        
        // Assign user if provided
        if (taskDto.getAssignedToId() != null) {
            User user = userRepository.findById(taskDto.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + taskDto.getAssignedToId()));
            task.setAssignedTo(user);
        }
        
        Task savedTask = taskRepository.save(task);
        return convertToDto(savedTask);
    }
    
    /**
     * Update an existing task.
     *
     * @param id task ID
     * @param taskDto updated task data
     * @return updated task DTO
     * @throws ResourceNotFoundException if task not found
     */
    @Transactional
    public TaskDto updateTask(Long id, TaskDto taskDto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setStatus(taskDto.getStatus());
        task.setPriority(taskDto.getPriority());
        
        // Update assigned user if provided
        if (taskDto.getAssignedToId() != null) {
            User user = userRepository.findById(taskDto.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + taskDto.getAssignedToId()));
            task.setAssignedTo(user);
        } else {
            task.setAssignedTo(null);
        }
        
        Task updatedTask = taskRepository.save(task);
        return convertToDto(updatedTask);
    }
    
    /**
     * Delete a task by ID.
     *
     * @param id task ID
     * @throws ResourceNotFoundException if task not found
     */
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }
    
    /**
     * Convert Task entity to TaskDto.
     *
     * @param task the task entity
     * @return task DTO
     */
    private TaskDto convertToDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        
        if (task.getAssignedTo() != null) {
            dto.setAssignedToId(task.getAssignedTo().getId());
            dto.setAssignedToUsername(task.getAssignedTo().getUsername());
        }
        
        return dto;
    }
}
