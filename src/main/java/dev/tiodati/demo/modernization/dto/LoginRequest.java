package dev.tiodati.demo.modernization.dto;

// MIGRATION NOTE: javax.validation.* will migrate to jakarta.validation.* in Spring Boot 3.x
import javax.validation.constraints.NotBlank;

/**
 * DTO for user login requests.
 */
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    public LoginRequest() {
    }
    
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
