package com.taller.security.config;

import com.taller.security.model.Role;
import com.taller.security.model.User;
import com.taller.security.repository.UserRepository;
import java.util.EnumSet;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class InitialAdminSeeder {
    /**
     * Crea el primer administrador real si el correo configurado aun no existe.
     */
    @Bean
    ApplicationRunner seedInitialAdmin(
            InitialAdminProperties properties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (!properties.isEnabled()) {
                return;
            }
            String email = properties.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmail(email)) {
                return;
            }

            User admin = new User();
            admin.setName(properties.getName().trim());
            admin.setEmail(email);
            admin.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
            admin.setRoles(EnumSet.of(Role.GERENTE));
            userRepository.save(admin);
        };
    }
}
