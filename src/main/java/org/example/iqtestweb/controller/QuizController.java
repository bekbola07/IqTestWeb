package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.entity.enums.QuizStatus;
import org.example.iqtestweb.service.AnswerOptionService;
import org.example.iqtestweb.service.QuestionService;
import org.example.iqtestweb.service.QuizService;
import org.example.iqtestweb.service.QuizTypeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizTypeService quizTypeService;
    private final QuestionService questionService;
    private final AnswerOptionService answerOptionService;

    // Quiz List
    @GetMapping("/list")
    public String listQuizzes(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("quizzes", quizService.getQuizzesByUserId(user.getUserId()));
        return "quiz-list";
    }

    // Create Quiz
    @GetMapping("/create")
    public String showCreateQuizForm(Model model) {
        model.addAttribute("quiz", new Quiz());
        model.addAttribute("quizTypes", quizTypeService.getAllQuizTypes());
        return "create-quiz";
    }

    @PostMapping("/create")
    public String createQuiz(@ModelAttribute Quiz quiz, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        quiz.setUser(user);
        quizService.saveQuiz(quiz);
        redirectAttributes.addFlashAttribute("success", "Quiz created successfully!");
        return "redirect:/quiz/list";
    }

    // Edit Quiz
    @GetMapping("/{id}/edit")
    public String showEditQuizForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        // Authorization check
        if (!isUserOwner(id, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this quiz.");
            return "redirect:/quiz/list";
        }
        model.addAttribute("quiz", quizService.getQuizById(id));
        model.addAttribute("quizTypes", quizTypeService.getAllQuizTypes());
        return "edit-quiz";
    }

    @PostMapping("/{id}/edit")
    public String editQuiz(@PathVariable Long id, @ModelAttribute Quiz quiz, HttpSession session, RedirectAttributes redirectAttributes) {
        // Authorization check
        if (!isUserOwner(id, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this quiz.");
            return "redirect:/quiz/list";
        }
        Quiz dbQuiz = quizService.getQuizById(id);
        User user = (User) session.getAttribute("user");
        dbQuiz.setId(id);
        dbQuiz.setUser(user);
        dbQuiz.setName(quiz.getName());
        dbQuiz.setQuizType(quiz.getQuizType());
        quizService.saveQuiz(dbQuiz);
        redirectAttributes.addFlashAttribute("success", "Quiz updated successfully!");
        return "redirect:/quiz/list";
    }

    // Delete Quiz
    @GetMapping("/{id}/delete")
    public String deleteQuiz(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(id, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete this quiz.");
            return "redirect:/quiz/list";
        }
        quizService.deleteQuiz(id);
        redirectAttributes.addFlashAttribute("success", "Quiz deleted successfully!");
        return "redirect:/quiz/list";
    }

    // Start/Stop Quiz
    @GetMapping("/{id}/start")
    public String startQuiz(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(id, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to modify this quiz.");
            return "redirect:/quiz/list";
        }
        Quiz quiz = quizService.getQuizById(id);
        quiz.setStatus(QuizStatus.STARTED);
        quizService.saveQuiz(quiz);
        return "redirect:/quiz/list";
    }

    @GetMapping("/{id}/stop")
    public String stopQuiz(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(id, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to modify this quiz.");
            return "redirect:/quiz/list";
        }
        Quiz quiz = quizService.getQuizById(id);
        quiz.setStatus(QuizStatus.STOPPED);
        quizService.saveQuiz(quiz);
        return "redirect:/quiz/list";
    }

    // Add Question to Quiz
    @GetMapping("/{quizId}/questions/add")
    public String showAddQuestionForm(@PathVariable Long quizId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(quizId, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to add questions to this quiz.");
            return "redirect:/quiz/list";
        }
        Quiz quiz = quizService.getQuizById(quizId);
        Question question = new Question();
        question.setQuiz(quiz);
        model.addAttribute("question", question);
        model.addAttribute("quiz", quiz);
        model.addAttribute("categories", questionService.getAllCategories());
        return "add-question-to-quiz";
    }

    @PostMapping("/{quizId}/questions/add")
    public String addQuestion(@PathVariable Long quizId, @ModelAttribute Question question,
                              @RequestParam List<String> optionTexts, @RequestParam(required = false) List<String> optionImageUrls,
                              @RequestParam int correctOptionIndex, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(quizId, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to add questions to this quiz.");
            return "redirect:/quiz/list";
        }
        Quiz quiz = quizService.getQuizById(quizId);
        question.setQuiz(quiz);
        Question savedQuestion = questionService.saveQuestion(question);

        List<AnswerOption> answerOptions = new ArrayList<>();
        for (int i = 0; i < optionTexts.size(); i++) {
            String imageUrl = (optionImageUrls != null && optionImageUrls.size() > i) ? optionImageUrls.get(i) : null;
            AnswerOption option = new AnswerOption(savedQuestion, optionTexts.get(i), imageUrl, i == correctOptionIndex, i);
            answerOptions.add(option);
        }
        answerOptionService.saveAll(answerOptions);

        redirectAttributes.addFlashAttribute("success", "Question added successfully!");
        return "redirect:/quiz/" + quizId + "/edit"; // Redirect back to edit quiz page
    }

    // Edit Question in Quiz
    @GetMapping("/{quizId}/questions/{questionId}/edit")
    public String showEditQuestionForm(@PathVariable Long quizId, @PathVariable Long questionId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(quizId, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit questions for this quiz.");
            return "redirect:/quiz/list";
        }
        Quiz quiz = quizService.getQuizById(quizId);
        Question question = questionService.getQuestionById(questionId);
        if (question == null || !question.getQuiz().getId().equals(quizId)) {
            redirectAttributes.addFlashAttribute("error", "Question not found or does not belong to this quiz.");
            return "redirect:/quiz/" + quizId + "/edit";
        }

        List<AnswerOption> answerOptions = answerOptionService.findByQuestionId(questionId);
        Integer correctOptionIndex = answerOptions.stream()
                .filter(AnswerOption::getIsCorrect)
                .findFirst()
                .map(AnswerOption::getOptionOrder)
                .orElse(0);

        model.addAttribute("question", question);
        model.addAttribute("answerOptions", answerOptions);
        model.addAttribute("quiz", quiz);
        model.addAttribute("categories", questionService.getAllCategories());
        model.addAttribute("correctOptionIndex", correctOptionIndex); // Add to model
        return "edit-question-for-quiz"; // New template for editing questions
    }

    @PostMapping("/{quizId}/questions/{questionId}/edit")
    public String editQuestion(@PathVariable Long quizId, @PathVariable Long questionId, @ModelAttribute Question question,
                               @RequestParam List<String> optionTexts, @RequestParam(required = false) List<String> optionImageUrls,
                               @RequestParam int correctOptionIndex, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(quizId, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit questions for this quiz.");
            return "redirect:/quiz/list";
        }
        Question existingQuestion = questionService.getQuestionById(questionId);
        if (existingQuestion == null || !existingQuestion.getQuiz().getId().equals(quizId)) {
            redirectAttributes.addFlashAttribute("error", "Question not found or does not belong to this quiz.");
            return "redirect:/quiz/" + quizId + "/edit";
        }

        // Update question details
        existingQuestion.setQuestionText(question.getQuestionText());
        existingQuestion.setQuestionCategory(question.getQuestionCategory());
        existingQuestion.setQuestionImageUrl(question.getQuestionImageUrl());
        existingQuestion.setQuestionType(question.getQuestionType());
        existingQuestion.setDifficultyLevel(question.getDifficultyLevel());
        existingQuestion.setTimeLimitSeconds(question.getTimeLimitSeconds());
        existingQuestion.setPoints(question.getPoints());
        questionService.saveQuestion(existingQuestion);

        // Soft delete old answer options and save new ones
        answerOptionService.softDeleteByQuestionId(questionId);
        List<AnswerOption> newAnswerOptions = new ArrayList<>();
        for (int i = 0; i < optionTexts.size(); i++) {
            String imageUrl = (optionImageUrls != null && optionImageUrls.size() > i) ? optionImageUrls.get(i) : null;
            AnswerOption option = new AnswerOption(existingQuestion, optionTexts.get(i), imageUrl, i == correctOptionIndex, i);
            newAnswerOptions.add(option);
        }
        answerOptionService.saveAll(newAnswerOptions);

        redirectAttributes.addFlashAttribute("success", "Question updated successfully!");
        return "redirect:/quiz/" + quizId + "/edit"; // Redirect back to edit quiz page
    }

    // Delete Question from Quiz
    @GetMapping("/{quizId}/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long quizId, @PathVariable Long questionId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(quizId, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete questions from this quiz.");
            return "redirect:/quiz/list";
        }
        Question question = questionService.getQuestionById(questionId);
        if (question == null || !question.getQuiz().getId().equals(quizId)) {
            redirectAttributes.addFlashAttribute("error", "Question not found or does not belong to this quiz.");
            return "redirect:/quiz/" + quizId + "/edit";
        }
        questionService.deleteQuestion(questionId);
        redirectAttributes.addFlashAttribute("success", "Question deleted successfully!");
        return "redirect:/quiz/" + quizId + "/edit"; // Redirect back to edit quiz page
    }

    // Helper for authorization
    private boolean isUserOwner(Long quizId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return false;
        }
        Quiz quiz = quizService.getQuizById(quizId);
        return quiz != null && quiz.getUser().getUserId().equals(user.getUserId());
    }
}
