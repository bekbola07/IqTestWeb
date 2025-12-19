package org.example.iqtestweb.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.entity.dto.CategoryDTO;
import org.example.iqtestweb.service.QuestionService;
import org.example.iqtestweb.service.QuizService;
import org.example.iqtestweb.service.QuizTypeService;
import org.example.iqtestweb.service.TestSessionService;
import org.example.iqtestweb.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
class AdminController {
    private final QuestionService questionService;
    private final UserService userService;
    private final TestSessionService sessionService;
    private final QuizTypeService quizTypeService;
    private final QuizService quizService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        List<User> users = userService.getAllUsers();
        List<TestSession> sessions = sessionService.getAllSessions();

        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalTests", sessions.size());
        model.addAttribute("userName", session.getAttribute("userName"));

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model, HttpSession session) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/users";
    }

    @GetMapping("/sessions")
    public String listSessions(Model model, HttpSession session) {
        model.addAttribute("sessions", sessionService.getAllSessions());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/sessions";
    }

    @GetMapping("/categories")
    public String listCategories(Model model, HttpSession session) {
        List<QuestionCategory> categories = questionService.getAllCategories();

        List<CategoryDTO> categoryDTOS = categories.stream()
                .map(category -> new CategoryDTO(category, questionService.getQuestionsByCategoryId(category.getCategoryId())))
                .collect(Collectors.toList());

        model.addAttribute("categories", categoryDTOS);
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/categories";
    }

    @GetMapping("/categories/add")
    public String showAddCategoryForm(Model model, HttpSession session) {
        model.addAttribute("category", new QuestionCategory());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/add-category";
    }

    @PostMapping("/categories/add")
    public String addCategory(@ModelAttribute QuestionCategory category,
                              RedirectAttributes redirectAttributes) {
        questionService.saveCategory(category);
        redirectAttributes.addFlashAttribute("success", "Category added successfully!");
        return "redirect:/admin/categories";
    }

    @GetMapping("/quiz-types")
    public String listQuizTypes(Model model, HttpSession session) {
        model.addAttribute("quizTypes", quizTypeService.getAllQuizTypes());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/quiz-types";
    }

    @GetMapping("/quiz-types/add")
    public String showAddQuizTypeForm(Model model, HttpSession session) {
        model.addAttribute("quizType", new QuizType());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/add-quiz-type";
    }

    @PostMapping("/quiz-types/add")
    public String addQuizType(@ModelAttribute QuizType quizType, RedirectAttributes redirectAttributes) {
        quizTypeService.saveQuizType(quizType);
        redirectAttributes.addFlashAttribute("success", "Quiz type added successfully!");
        return "redirect:/admin/quiz-types";
    }

    @GetMapping("/quiz-types/edit/{id}")
    public String showEditQuizTypeForm(@PathVariable Long id, Model model, HttpSession session) {
        model.addAttribute("quizType", quizTypeService.getQuizTypeById(id));
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/edit-quiz-type";
    }

    @PostMapping("/quiz-types/edit/{id}")
    public String editQuizType(@PathVariable Long id, @ModelAttribute QuizType quizType, RedirectAttributes redirectAttributes) {
        quizType.setId(id);
        quizTypeService.saveQuizType(quizType);
        redirectAttributes.addFlashAttribute("success", "Quiz type updated successfully!");
        return "redirect:/admin/quiz-types";
    }

    @GetMapping("/quiz-types/delete/{id}")
    public String deleteQuizType(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        quizTypeService.deleteQuizType(id);
        redirectAttributes.addFlashAttribute("success", "Quiz type deleted successfully!");
        return "redirect:/admin/quiz-types";
    }

    @GetMapping("/quizzes")
    public String listQuizzes(Model model, HttpSession session) {
        model.addAttribute("quizzes", quizService.getAllQuizzes());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/quizzes";
    }
}
