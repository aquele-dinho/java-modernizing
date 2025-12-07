package dev.tiodati.demo.modernization.repository;

import dev.tiodati.demo.modernization.domain.Task;
import dev.tiodati.demo.modernization.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Task entity.
 * Provides database access methods for task management.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    /**
     * Find all tasks assigned to a specific user with pagination.
     *
     * @param userId the ID of the user
     * @param pageable pagination information
     * @return Page of tasks assigned to the user
     */
    Page<Task> findByAssignedToId(Long userId, Pageable pageable);
    
    /**
     * Find all tasks by status with pagination.
     *
     * @param status the task status to filter by
     * @param pageable pagination information
     * @return Page of tasks with the specified status
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
}
