package dev.tiodati.demo.modernization.integration;

import dev.tiodati.demo.modernization.domain.Priority;
import dev.tiodati.demo.modernization.domain.TaskStatus;
import dev.tiodati.demo.modernization.dto.JwtResponse;
import dev.tiodati.demo.modernization.dto.LoginRequest;
import dev.tiodati.demo.modernization.dto.TaskDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for security and authorization.
 * Tests verify JWT authentication, role-based access control, and public endpoint access.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecurityIntegrationTest {
    
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
        
        // Login as admin
        LoginRequest adminLogin = new LoginRequest("admin", "password");
        ResponseEntity<JwtResponse> adminResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                adminLogin,
                JwtResponse.class
        );
        adminToken = adminResponse.getBody().getToken();
        
        // Login as regular user
        LoginRequest userLogin = new LoginRequest("user", "password");
        ResponseEntity<JwtResponse> userResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                userLogin,
                JwtResponse.class
        );
        userToken = userResponse.getBody().getToken();
    }
    
    @Test
    void testPublicEndpointsAccessibleWithoutAuthentication() {
        // Test login endpoint
        LoginRequest loginRequest = new LoginRequest("admin", "password");
        ResponseEntity<JwtResponse> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginRequest,
                JwtResponse.class
        );
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        assertNotNull(loginResponse.getBody().getToken());
    }
    
    @Test
    void testProtectedEndpointsRejectRequestsWithoutJWT() {
        // Try to access tasks without JWT token
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/tasks",
                String.class
        );
        
        // Should be rejected (401 Unauthorized or 403 Forbidden)
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                   response.getStatusCode() == HttpStatus.FORBIDDEN);
    }
    
    @Test
    void testProtectedEndpointsRejectInvalidJWT() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.jwt.token");
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.GET,
                entity,
                String.class
        );
        
        // Assert - Should be rejected
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                   response.getStatusCode() == HttpStatus.FORBIDDEN);
    }
    
    @Test
    void testProtectedEndpointsAcceptValidJWT() {
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
    }
    
    @Test
    void testAdminOnlyEndpointsRejectRegularUsers() {
        // Arrange - Create a task first (with admin)
        TaskDto newTask = new TaskDto();
        newTask.setTitle("Task for Security Test");
        newTask.setDescription("Testing admin-only delete");
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
    
    @Test
    void testAdminOnlyEndpointsAllowAdminUsers() {
        // Arrange - Create a task
        TaskDto newTask = new TaskDto();
        newTask.setTitle("Task for Admin Delete Test");
        newTask.setDescription("Should be deletable by admin");
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
        
        // Act - Delete with admin token
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
    }
    
    @Test
    void testInvalidCredentialsRejected() {
        // Arrange
        LoginRequest badLogin = new LoginRequest("admin", "wrongpassword");
        
        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                badLogin,
                String.class
        );
        
        // Assert - Should be rejected (401 or 403)
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                   response.getStatusCode() == HttpStatus.FORBIDDEN ||
                   response.getStatusCode() == HttpStatus.BAD_REQUEST);
    }
    
    @Test
    void testValidJWTContainsCorrectUserInfo() {
        // Arrange & Act
        LoginRequest loginRequest = new LoginRequest("admin", "password");
        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginRequest,
                JwtResponse.class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getToken());
        assertEquals("admin", response.getBody().getUsername());
        assertEquals("admin@demo.com", response.getBody().getEmail());
        assertEquals("Bearer", response.getBody().getType());
    }
    
    @Test
    void testJWTWorksForMultipleRequests() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        // Act - Make multiple requests with same token
        ResponseEntity<String> response1 = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.GET,
                entity,
                String.class
        );
        
        ResponseEntity<String> response2 = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.GET,
                entity,
                String.class
        );
        
        ResponseEntity<TaskDto> response3 = restTemplate.exchange(
                baseUrl + "/api/tasks/1",
                HttpMethod.GET,
                entity,
                TaskDto.class
        );
        
        // Assert - All requests should succeed
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(HttpStatus.OK, response3.getStatusCode());
    }
    
    @Test
    void testExpiredOrMalformedTokenRejected() {
        // Arrange - Use completely invalid token format
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("this-is-not-a-valid-jwt");
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/tasks",
                HttpMethod.GET,
                entity,
                String.class
        );
        
        // Assert
        assertTrue(response.getStatusCode() == HttpStatus.UNAUTHORIZED ||
                   response.getStatusCode() == HttpStatus.FORBIDDEN);
    }
    
    @Test
    void testRegularUserCanAccessOwnTasks() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        // Act - Get tasks for user with ID 2 (regular user in data.sql)
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/tasks/user/2",
                HttpMethod.GET,
                entity,
                String.class
        );
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    @Test
    void testRegularUserCanCreateTask() {
        // Arrange
        TaskDto newTask = new TaskDto();
        newTask.setTitle("User Created Task");
        newTask.setDescription("Created by regular user");
        newTask.setStatus(TaskStatus.OPEN);
        newTask.setPriority(Priority.LOW);
        newTask.setAssignedToId(2L);
        
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
        assertEquals("User Created Task", response.getBody().getTitle());
    }
    
    @Test
    void testRegularUserCanUpdateTask() {
        // Arrange - Create a task first
        TaskDto newTask = new TaskDto();
        newTask.setTitle("Task to Update");
        newTask.setDescription("Original");
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
        updateTask.setTitle("Updated by User");
        updateTask.setDescription("Modified");
        updateTask.setStatus(TaskStatus.IN_PROGRESS);
        updateTask.setPriority(Priority.MEDIUM);
        
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
        assertEquals("Updated by User", response.getBody().getTitle());
    }
}
