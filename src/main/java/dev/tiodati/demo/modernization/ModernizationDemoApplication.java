package dev.tiodati.demo.modernization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Java Modernization Demo.
 * 
 * This application demonstrates migration from:
 * - Java 11 → 17 → 21
 * - Spring Boot 2.4 → 3.x
 * 
 * Current state: Java 11 + Spring Boot 2.4.13 (Baseline for migration demo)
 */
@SpringBootApplication
public class ModernizationDemoApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ModernizationDemoApplication.java, args);
    }
}
