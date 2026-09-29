package com.taller.security;

import com.taller.security.config.InitialAdminProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InitialAdminProperties.class)
public class TallerSecurityApplication {
    public static void main(String[] args) {
        SpringApplication.run(TallerSecurityApplication.class, args);
    }
}
