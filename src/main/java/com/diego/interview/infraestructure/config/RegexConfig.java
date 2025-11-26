package com.diego.interview.infraestructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.regex.Pattern;

@Configuration
public class RegexConfig {

    @Bean
    public Pattern emailPattern(
            @Value("${app.security.email-regex}") String emailRegex) {
        return Pattern.compile(emailRegex);
    }

    @Bean
    public Pattern passwordPattern(
            @Value("${app.security.password-regex}") String passwordRegex) {
        return Pattern.compile(passwordRegex);
    }
}
