package dev.tiodati.demo.modernization.controller;

import dev.tiodati.demo.modernization.dto.TaskDto;
import dev.tiodati.demo.modernization.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for task management operations.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    /**
     * Get all tasks with pagination.
     *
     * @param pageable pagination parameters
     * @return page of tasks
     */
    @GetMapping
    public ResponseEntity<Page<TaskDto>> getAllTasks(Pageable pageable) {
        Page<TaskDto> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get task by ID.
     *
     * @param id task ID
     * @return task DTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        TaskDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }
    
    /**
     * Get tasks assigned to a specific user.
     *
     * @param userId user ID
     * @param pageable pagination parameters
     * @return page of tasks
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TaskDto>> getTasksByUserId(
            @PathVariable Long userId, Pageable pageable) {
        Page<TaskDto> tasks = taskService.getTasksByUserId(userId, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Create a new task.
     *
     * @param taskDto task data
     * @return created task
     */
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto) {
        TaskDto createdTask = taskService.createTask(taskDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }
    
    /**
     * Update an existing task.
     *
     * @param id task ID
     * @param taskDto updated task data
     * @return updated task
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long id, @Valid @RequestBody TaskDto taskDto) {
        TaskDto updatedTask = taskService.updateTask(id, taskDto);
        return ResponseEntity.ok(updatedTask);
    }
    
    /**
     * Delete a task.
     * Requires ADMIN role.
     *
     * @param id task ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
