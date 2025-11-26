package org.example.iqtestweb.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.entity.dto.CategoryDTO;
import org.example.iqtestweb.service.QuestionService;
import org.example.iqtestweb.service.TestSessionService;
import org.example.iqtestweb.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
class AdminController {
    private final QuestionService questionService;
    private final UserService userService;
    private final TestSessionService sessionService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        List<User> users = userService.getAllUsers();
        List<Question> questions = questionService.getAllQuestions();
        List<TestSession> sessions = sessionService.getAllSessions();

        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalQuestions", questions.size());
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

    @GetMapping("/questions")
    public String listQuestions(Model model, HttpSession session) {
        model.addAttribute("questions", questionService.getAllQuestions());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/questions";
    }

    @GetMapping("/questions/add")
    public String showAddQuestionForm(Model model, HttpSession session) {
        List<QuestionCategory> categories = questionService.getAllCategories();

        model.addAttribute("question", new Question());
        model.addAttribute("categories", categories);
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/add-question";
    }

    @PostMapping("/questions/add")
    public String addQuestion(@ModelAttribute("question") Question question,
                              @RequestParam List<String> optionTexts,
                              @RequestParam List<String> optionImageUrls,
                              @RequestParam int correctOptionIndex,
                              RedirectAttributes redirectAttributes) {

        List<AnswerOption> answerOptions = new ArrayList<>();
        for (int i = 0; i < optionTexts.size(); i++) {
            String imageUrl = (optionImageUrls.size() > i) ? optionImageUrls.get(i) : null;
            AnswerOption option = new AnswerOption(question, optionTexts.get(i), imageUrl, i == correctOptionIndex, i);
            answerOptions.add(option);
        }
        question.setAnswerOptions(answerOptions);
        questionService.saveQuestion(question);

        redirectAttributes.addFlashAttribute("success", "Question added successfully!");
        return "redirect:/admin/questions";
    }

    @GetMapping("/questions/edit/{id}")
    public String showEditQuestionForm(@PathVariable Long id, Model model, HttpSession session) {
        Question question = questionService.getQuestionById(id);
        List<QuestionCategory> categories = questionService.getAllCategories();

        model.addAttribute("question", question);
        model.addAttribute("categories", categories);
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/edit-question";
    }

    @PostMapping("/questions/edit/{id}")
    public String editQuestion(@PathVariable Long id,
                               @ModelAttribute Question question,
                               RedirectAttributes redirectAttributes) {

        questionService.updateQuestion(id, question);
        redirectAttributes.addFlashAttribute("success", "Question updated successfully!");
        return "redirect:/admin/questions";
    }

    @GetMapping("/questions/delete/{id}")
    public String deleteQuestion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        questionService.deleteQuestion(id);
        redirectAttributes.addFlashAttribute("success", "Question deleted successfully!");
        return "redirect:/admin/questions";
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

        // This approach avoids the N+1 query problem of fetching questions for each category individually.
        // A more advanced solution would be a custom query in the repository.
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
}
