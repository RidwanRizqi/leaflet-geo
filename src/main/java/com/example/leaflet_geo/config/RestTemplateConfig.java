package com.example.leaflet_geo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for RestTemplate
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Configure RestTemplate bean for HTTP client calls
     * 
     * @return RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}