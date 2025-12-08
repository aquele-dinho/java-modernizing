package dev.tiodati.demo.modernization.config;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * REST Client Configuration using Apache HttpClient 4.x.
 * 
 * MIGRATION NOTE: This configuration demonstrates legacy patterns that will need updates:
 * 1. Apache HttpClient 4.x will be replaced with 5.x in Spring Boot 3.x
 * 2. Package names will change: org.apache.http.* (4.x) → org.apache.hc.client5.* (5.x)
 * 3. TrustAllStrategy is insecure and used only for demonstration purposes
 * 
 * This intentional legacy code serves as baseline for migration demonstration.
 */
@Configuration
public class RestClientConfig {
    
    /**
     * Configure RestTemplate with custom HttpClient 4.x settings.
     * MIGRATION NOTE: HttpComponentsClientHttpRequestFactory constructor and API will change in SB 3.x
     *
     * @return configured RestTemplate bean
     */
    @Bean
    public RestTemplate restTemplate() throws Exception {
        // ⚠️ SECURITY WARNING: TrustAllStrategy is INSECURE and disables SSL certificate validation!
        // This accepts ANY certificate, including invalid, expired, or self-signed certificates.
        // NEVER use this in production! This pattern is intentionally included for migration demonstration.
        // 
        // MIGRATION NOTE: In production, either:
        // 1. Use default SSL validation (remove custom SSLContext)
        // 2. Load specific trusted certificates from a keystore
        // 3. Use proper certificate validation with custom trust managers
        // MIGRATION NOTE:
        // For simplicity in this demo, we now use the default HttpClient 5 configuration.
        // In a real migration, replace this with a properly configured CloseableHttpClient
        // using connection pooling and SSL/TLS settings that meet your security requirements.
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        // Create request factory with timeouts
        HttpComponentsClientHttpRequestFactory requestFactory = 
                new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(5000);
        // Manual migration to `SocketConfig.Builder.setSoTimeout(Timeout)` necessary; see: https://docs.spring.io/spring-framework/docs/6.0.0/javadoc-api/org/springframework/http/client/HttpComponentsClientHttpRequestFactory.html#setReadTimeout(int)
        requestFactory.setReadTimeout(5000);
        
        return new RestTemplate(requestFactory);
    }
}
