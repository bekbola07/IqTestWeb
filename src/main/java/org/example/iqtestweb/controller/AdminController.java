package org.example.iqtestweb.controller;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.entity.dto.CategoryDTO;
import org.example.iqtestweb.entity.dto.QuestionDTO;
import org.example.iqtestweb.service.QuestionService;
import org.example.iqtestweb.service.TestSessionService;
import org.example.iqtestweb.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
class AdminController {
    private final QuestionService questionService;
    private final UserService userService;
    private final TestSessionService sessionService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }

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
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/users";
    }

    @GetMapping("/questions")
    public String listQuestions(Model model, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("questions", questionService.getAllQuestions());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/questions";
    }

    @GetMapping("/questions/add")
    public String showAddQuestionForm(Model model, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }

        List<QuestionCategory> categories = questionService.getAllCategories();

        model.addAttribute("question", new Question());
        model.addAttribute("categories", categories);
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/add-question";
    }

    @PostMapping("/questions/add")
    public String addQuestion(@ModelAttribute Question question,
                              @RequestParam List<String> optionTexts,
                              @RequestParam int correctOptionIndex,
                              RedirectAttributes redirectAttributes) {

        question.setIsActive(true);
        Question savedQuestion = questionService.saveQuestion(question);

        List<AnswerOption> savedAnswerOptions = new ArrayList<>();
        questionService.saveQuestion(savedQuestion);
        for (int i = 0; i < optionTexts.size(); i++) {
            AnswerOption option = new AnswerOption();
            option.setQuestion(savedQuestion);
            option.setOptionText(optionTexts.get(i));
            option.setIsCorrect(i == correctOptionIndex);
            option.setOptionOrder(i);
            questionService.saveAnswerOption(option);
//            savedQuestion.getAnswerOptions().add(option);
            savedAnswerOptions.add(option);
        }

        redirectAttributes.addFlashAttribute("success", "Question added successfully!");
        return "redirect:/admin/questions";
    }

    @GetMapping("/questions/edit/{id}")
    public String showEditQuestionForm(@PathVariable Long id, Model model, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }

        Question question = questionService.getQuestionById(id);

        List<AnswerOption> optionsForQuestion = questionService.getOptionsForQuestion(question.getQuestionId());
        QuestionDTO questionDTO = new QuestionDTO(question, optionsForQuestion);

        List<QuestionCategory> categories = questionService.getAllCategories();

        model.addAttribute("question", questionDTO);
        model.addAttribute("categories", categories);
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/edit-question";
    }

    @PostMapping("/questions/edit/{id}")
    public String editQuestion(@PathVariable Long id,
                               @ModelAttribute Question question,
                               RedirectAttributes redirectAttributes) {
        question.setQuestionId(id);
        questionService.saveQuestion(question);
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
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("sessions", sessionService.getAllSessions());
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/sessions";
    }

    @GetMapping("/categories")
    public String listCategories(Model model, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }

        List<CategoryDTO> categoryDTOS = new ArrayList<>();
        List<QuestionCategory> categories = questionService.getAllCategories();

        for (QuestionCategory category : categories) {
            categoryDTOS.add(new CategoryDTO(category, questionService.getQuestionsByCategoryId(category.getCategoryId())));
        }


        model.addAttribute("categories", categoryDTOS);
        model.addAttribute("userName", session.getAttribute("userName"));
        return "admin/categories";
    }

    @GetMapping("/categories/add")
    public String showAddCategoryForm(Model model, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/dashboard";
        }

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