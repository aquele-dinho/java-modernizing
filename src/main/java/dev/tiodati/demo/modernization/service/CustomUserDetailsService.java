package dev.tiodati.demo.modernization.service;

import dev.tiodati.demo.modernization.domain.User;
import dev.tiodati.demo.modernization.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * Loads user details from the database for authentication.
 * 
 * <p>This service is used by Spring Security to load user-specific data
 * during authentication. It converts the application's User entity into
 * Spring Security's UserDetails object with proper role mappings.</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Load user details by username for authentication.
     * 
     * <p>This method:
     * <ol>
     *   <li>Retrieves user from database by username</li>
     *   <li>Parses comma-separated roles and converts to authorities</li>
     *   <li>Prefixes each role with "ROLE_" as required by Spring Security</li>
     *   <li>Returns Spring Security UserDetails object</li>
     * </ol>
     * 
     * @param username the username identifying the user
     * @return fully populated UserDetails object
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        List<SimpleGrantedAuthority> authorities = Arrays.stream(user.getRoles().split(","))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                .collect(Collectors.toList());
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
