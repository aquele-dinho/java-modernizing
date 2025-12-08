package dev.tiodati.demo.modernization.integration;

import dev.tiodati.demo.modernization.domain.Priority;
import dev.tiodati.demo.modernization.domain.TaskStatus;
import dev.tiodati.demo.modernization.dto.JwtResponse;
import dev.tiodati.demo.modernization.dto.LoginRequest;
import dev.tiodati.demo.modernization.dto.RegisterRequest;
import dev.tiodati.demo.modernization.dto.TaskDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Task Controller endpoints.
 * Tests use TestRestTemplate to make actual HTTP requests to the running application.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskControllerIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    private String baseUrl;
    private String adminToken;
    private String userToken;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        
        // Login as admin to get admin JWT token
        LoginRequest adminLogin = new LoginRequest("admin", "password");
        ResponseEntity<JwtResponse> adminResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                adminLogin,
                JwtResponse.class
        );
        adminToken = adminResponse.getBody().getToken();
        
        // Login as regular user to get user JWT token
        LoginRequest userLogin = new LoginRequest("user", "password");
        ResponseEntity<JwtResponse> userResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                userLogin,
                JwtResponse.class
        );
        userToken = userResponse.getBody().getToken();
    }
    
    @Test
    void testRegisterNewUser() {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "newuser",
                "newuser@example.com",
                "password123"
        );
        
        // Act
        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/register",
                request,
                JwtResponse.class
        );
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        assertEquals("newuser", response.getBody().getUsername());
        assertEquals("newuser@example.com", response.getBody().getEmail());
        assertEquals("Bearer", response.getBody().getType());
    }
    
    @Test
    void testLoginWithValidCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest("admin", "password");
        
        // Act
        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                request,
                JwtResponse.class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        assertEquals("admin", response.getBody().getUsername());
    }
    
    @Test
    void testGetAllTasksWithValidToken() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.GET,
                entity,
                String.class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("content"));
    }
    
    @Test
    void testGetTaskByIdWithValidToken() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<TaskDto> response = restTemplate.exchange(
                baseUrl + "/api/tasks/1",
                HttpMethod.GET,
                entity,
                TaskDto.class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertNotNull(response.getBody().getTitle());
    }
    
    @Test
    void testCreateTaskWithValidToken() {
        // Arrange
        TaskDto newTask = new TaskDto();
        newTask.setTitle("Integration Test Task");
        newTask.setDescription("Created during integration test");
        newTask.setStatus(TaskStatus.OPEN);
        newTask.setPriority(Priority.HIGH);
        newTask.setAssignedToId(1L);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TaskDto> entity = new HttpEntity<>(newTask, headers);
        
        // Act
        ResponseEntity<TaskDto> response = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.POST,
                entity,
                TaskDto.class
        );
        
        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Integration Test Task", response.getBody().getTitle());
        assertEquals("Created during integration test", response.getBody().getDescription());
        assertEquals(TaskStatus.OPEN, response.getBody().getStatus());
        assertEquals(Priority.HIGH, response.getBody().getPriority());
    }
    
    @Test
    void testUpdateTaskWithValidToken() {
        // Arrange - First create a task
        TaskDto newTask = new TaskDto();
        newTask.setTitle("Task to Update");
        newTask.setDescription("Original description");
        newTask.setStatus(TaskStatus.OPEN);
        newTask.setPriority(Priority.LOW);
        
        HttpHeaders createHeaders = new HttpHeaders();
        createHeaders.setBearerAuth(userToken);
        createHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TaskDto> createEntity = new HttpEntity<>(newTask, createHeaders);
        
        ResponseEntity<TaskDto> createResponse = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.POST,
                createEntity,
                TaskDto.class
        );
        Long taskId = createResponse.getBody().getId();
        
        // Act - Update the task
        TaskDto updateTask = new TaskDto();
        updateTask.setTitle("Updated Task Title");
        updateTask.setDescription("Updated description");
        updateTask.setStatus(TaskStatus.IN_PROGRESS);
        updateTask.setPriority(Priority.HIGH);
        
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setBearerAuth(userToken);
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TaskDto> updateEntity = new HttpEntity<>(updateTask, updateHeaders);
        
        ResponseEntity<TaskDto> response = restTemplate.exchange(
                baseUrl + "/api/tasks/" + taskId,
                HttpMethod.PUT,
                updateEntity,
                TaskDto.class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(taskId, response.getBody().getId());
        assertEquals("Updated Task Title", response.getBody().getTitle());
        assertEquals("Updated description", response.getBody().getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, response.getBody().getStatus());
        assertEquals(Priority.HIGH, response.getBody().getPriority());
    }
    
    @Test
    void testDeleteTaskWithAdminToken() {
        // Arrange - First create a task
        TaskDto newTask = new TaskDto();
        newTask.setTitle("Task to Delete");
        newTask.setDescription("Will be deleted");
        newTask.setStatus(TaskStatus.OPEN);
        newTask.setPriority(Priority.LOW);
        
        HttpHeaders createHeaders = new HttpHeaders();
        createHeaders.setBearerAuth(adminToken);
        createHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TaskDto> createEntity = new HttpEntity<>(newTask, createHeaders);
        
        ResponseEntity<TaskDto> createResponse = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.POST,
                createEntity,
                TaskDto.class
        );
        Long taskId = createResponse.getBody().getId();
        
        // Act - Delete the task with admin token
        HttpHeaders deleteHeaders = new HttpHeaders();
        deleteHeaders.setBearerAuth(adminToken);
        HttpEntity<Void> deleteEntity = new HttpEntity<>(deleteHeaders);
        
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/tasks/" + taskId,
                HttpMethod.DELETE,
                deleteEntity,
                Void.class
        );
        
        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        
        // Verify task is deleted (expect 404 error response)
        ResponseEntity<String> getResponse = restTemplate.exchange(
                baseUrl + "/api/tasks/" + taskId,
                HttpMethod.GET,
                deleteEntity,
                String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
    
    @Test
    void testDeleteTaskRejectsNonAdminUser() {
        // Arrange - First create a task
        TaskDto newTask = new TaskDto();
        newTask.setTitle("Task Protected from Non-Admin Delete");
        newTask.setDescription("Should not be deletable by regular user");
        newTask.setStatus(TaskStatus.OPEN);
        newTask.setPriority(Priority.MEDIUM);
        
        HttpHeaders createHeaders = new HttpHeaders();
        createHeaders.setBearerAuth(adminToken);
        createHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TaskDto> createEntity = new HttpEntity<>(newTask, createHeaders);
        
        ResponseEntity<TaskDto> createResponse = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.POST,
                createEntity,
                TaskDto.class
        );
        Long taskId = createResponse.getBody().getId();
        
        // Act - Try to delete with regular user token
        HttpHeaders deleteHeaders = new HttpHeaders();
        deleteHeaders.setBearerAuth(userToken);
        HttpEntity<Void> deleteEntity = new HttpEntity<>(deleteHeaders);
        
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/tasks/" + taskId,
                HttpMethod.DELETE,
                deleteEntity,
                Void.class
        );
        
        // Assert - Should be rejected (either FORBIDDEN or BAD_REQUEST depending on security configuration)
        assertTrue(response.getStatusCode() == HttpStatus.FORBIDDEN ||
                   response.getStatusCode() == HttpStatus.BAD_REQUEST);
    }
    
    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
