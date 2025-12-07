package dev.tiodati.demo.modernization.config;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

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
        // Create SSL context that trusts all certificates (INSECURE - for demo only)
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadTrustMaterial(new TrustAllStrategy())
                .build();
        
        // Create custom SSL socket factory
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        
        // Build HttpClient with custom SSL configuration and connection pooling
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(20)
                .build();
        
        // Create request factory with timeouts
        HttpComponentsClientHttpRequestFactory requestFactory = 
                new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        
        return new RestTemplate(requestFactory);
    }
}
