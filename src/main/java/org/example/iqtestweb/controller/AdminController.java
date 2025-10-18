package org.example.iqtestweb.controller;


import lombok.NoArgsConstructor;
import org.example.iqtestweb.entity.Question;
import org.example.iqtestweb.service.QuestionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@NoArgsConstructor(force = true)
class AdminController {
    private final QuestionService questionService;

    @GetMapping("/questions")
    public String listQuestions(Model model) {
        model.addAttribute("questions", questionService.getAllActiveQuestions());
        return "admin/questions";
    }

    @GetMapping("/questions/add")
    public String showAddQuestionForm(Model model) {
        model.addAttribute("question", new Question());
        return "admin/add-question";
    }

    @PostMapping("/questions/add")
    public String addQuestion(@ModelAttribute Question question) {
        questionService.saveQuestion(question);
        return "redirect:/admin/questions";
    }
}