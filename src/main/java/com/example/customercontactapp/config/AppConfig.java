package com.example.customercontactapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper getObectMapper() {
        return new ObjectMapper();
    }
    
}
