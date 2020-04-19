package com.dchampion.framework;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class FrameworkConfig {

    @Value("${framework.hash-work-factor:12}")
    private String value;

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(Integer.parseInt(value));
    }
}