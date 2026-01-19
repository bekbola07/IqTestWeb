package org.example.iqtestweb.runner;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.QuestionCategory;
import org.example.iqtestweb.entity.QuizType;
import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.entity.enums.UserRole;
import org.example.iqtestweb.repository.QuestionCategoryRepository;
import org.example.iqtestweb.repository.QuizTypeRepository;
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
    private final QuestionCategoryRepository questionCategoryRepository;
    private final QuizTypeRepository quizTypeRepository;

    @Override
    public void run(String... args) {
        System.out.println("ddl-auto=create detected. Initializing data...");

        // 1. Create Admin User
        User admin = new User();
        admin.setUsername("admin");
        admin.setPasswordHash(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@mail.com");
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);
        System.out.println("Admin user created successfully: username=admin");

        // 2. Create Question Categories
        createCategory("Og‘zaki (til) intellekt");
        createCategory("Mantiqiy yoki matematik fikrlash");
        createCategory("Fazoviy (ko‘rish orqali) intellekt");
        createCategory("Xotira va eslab qolish");
        createCategory("Qayta ishlash tezligi");
        createCategory("Abstrakt fikrlash");
        createCategory("Umumiy bilim / ma’lumot");
        createCategory("Hissiy yoki ijtimoiy intellekt");
        System.out.println("Question categories initialized.");

        // 3. Create Quiz Types
        createQuizType("Standard IQ Test");
        createQuizType("Practice Quiz");
        System.out.println("Quiz types initialized.");
    }

    private void createCategory(String name) {
        QuestionCategory category = new QuestionCategory();
        category.setCategoryName(name);
        questionCategoryRepository.save(category);
    }

    private void createQuizType(String name) {
        QuizType type = new QuizType();
        type.setName(name);
        quizTypeRepository.save(type);
    }
}
