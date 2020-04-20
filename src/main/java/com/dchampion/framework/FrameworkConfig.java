package com.dchampion.framework;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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
        SecureRandom sr = null;
        try {
            sr = SecureRandom.getInstance("Windows-PRNG");
        } catch (NoSuchAlgorithmException nsae) {
            sr = new SecureRandom();
        }
        return new BCryptPasswordEncoder(Integer.parseInt(value), sr);
    }
}