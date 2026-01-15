package org.example.iqtestweb.runner;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.entity.enums.UserRole;
import org.example.iqtestweb.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.jpa.hibernate.ddl-auto", havingValue = "create")
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        System.out.println("ddl-auto=create detected. Initializing Admin user...");

        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@mail.com");
        admin.setRole(UserRole.ADMIN); // Adjust this field based on your User entity

        userRepository.save(admin);
        System.out.println("Admin user created successfully: username=admin");
    }
}