package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.entity.dto.QuizDurationDto;
import org.example.iqtestweb.entity.enums.QuestionType;
import org.example.iqtestweb.entity.enums.QuizStatus;
import org.example.iqtestweb.entity.enums.QuizTimeType;
import org.example.iqtestweb.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizTypeService quizTypeService;
    private final QuestionService questionService;
    private final AnswerOptionService answerOptionService;
    private final AttachmentService attachmentService;

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
        model.addAttribute("durationDto", new QuizDurationDto());
        return "create-quiz";
    }

    @PostMapping("/create")
    public String createQuiz(@ModelAttribute Quiz quiz,
                             @ModelAttribute QuizDurationDto durationDto,
                             @RequestParam(value = "file", required = false) MultipartFile file,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        quiz.setUser(user);
        
        // Normalize duration
        if (quiz.getTimeType() == QuizTimeType.TOTAL_TIME) {
            int totalSeconds = durationDto.toTotalSeconds();
            if (totalSeconds <= 0) {
                redirectAttributes.addFlashAttribute("error", "Please specify a valid duration (at least 1 second).");
                return "redirect:/quiz/create";
            }
            quiz.setTimeLimitSeconds(totalSeconds);
        } else {
            quiz.setTimeLimitSeconds(null);
        }

        if (file != null && !file.isEmpty()) {
            Attachment attachment = attachmentService.saveAttachment(file);
            quiz.setAttachment(attachment);
        }
        quizService.saveQuiz(quiz);
        redirectAttributes.addFlashAttribute("success", "Quiz created successfully!");
        return "redirect:/quiz/list";
    }

    // Edit Quiz
    @GetMapping("/{id}/edit")
    public String showEditQuizForm(@PathVariable Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(id, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this quiz.");
            return "redirect:/quiz/list";
        }
        Quiz quiz = quizService.getQuizById(id);
        model.addAttribute("quiz", quiz);
        model.addAttribute("quizTypes", quizTypeService.getAllQuizTypes());
        model.addAttribute("categories", questionService.getAllCategories());
        model.addAttribute("newQuestion", new Question());
        
        // Pre-fill duration DTO
        model.addAttribute("durationDto", QuizDurationDto.fromTotalSeconds(quiz.getTimeLimitSeconds()));
        
        // Pass flag to disable inputs if STARTED
        model.addAttribute("isPublished", quiz.getStatus() == QuizStatus.STARTED);
        
        return "edit-quiz";
    }

    @PostMapping("/{id}/edit")
    public String editQuiz(@PathVariable Long id, @ModelAttribute Quiz quiz,
                           @ModelAttribute QuizDurationDto durationDto,
                           @RequestParam(value = "file", required = false) MultipartFile file,
                           @RequestParam(value = "removeImage", required = false) Boolean removeImage,
                           HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(id, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this quiz.");
            return "redirect:/quiz/list";
        }
        
        // Normalize duration
        if (quiz.getTimeType() == QuizTimeType.TOTAL_TIME) {
            int totalSeconds = durationDto.toTotalSeconds();
            if (totalSeconds <= 0) {
                redirectAttributes.addFlashAttribute("error", "Please specify a valid duration (at least 1 second).");
                return "redirect:/quiz/" + id + "/edit";
            }
            quiz.setTimeLimitSeconds(totalSeconds);
        } else {
            quiz.setTimeLimitSeconds(null);
        }
        
        Attachment newAttachment = null;
        if (file != null && !file.isEmpty()) {
            newAttachment = attachmentService.saveAttachment(file);
        }

        try {
            quizService.updateQuizSettings(id, quiz, newAttachment, Boolean.TRUE.equals(removeImage));
            redirectAttributes.addFlashAttribute("success", "Quiz updated successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while updating the quiz.");
        }
        
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
        try {
            quizService.startQuiz(id);
            redirectAttributes.addFlashAttribute("success", "Quiz started successfully!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/quiz/list";
    }

    @GetMapping("/{id}/stop")
    public String stopQuiz(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(id, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to modify this quiz.");
            return "redirect:/quiz/list";
        }
        quizService.stopQuiz(id);
        redirectAttributes.addFlashAttribute("success", "Quiz stopped successfully!");
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
        if (quiz.getStatus() == QuizStatus.STARTED) {
             redirectAttributes.addFlashAttribute("error", "Cannot add questions to a started quiz. Stop it first.");
             return "redirect:/quiz/" + quizId + "/edit";
        }
        
        Question question = new Question();
        question.setQuiz(quiz);
        model.addAttribute("question", question);
        model.addAttribute("quiz", quiz);
        model.addAttribute("categories", questionService.getAllCategories());
        return "add-question-to-quiz";
    }

    @PostMapping("/{quizId}/questions/add")
    public String addQuestion(@PathVariable Long quizId, @ModelAttribute Question question,
                              @RequestParam List<String> optionTexts,
                              @RequestParam(value = "questionImage", required = false) MultipartFile questionImage,
                              @RequestParam(value = "optionImages", required = false) List<MultipartFile> optionImages,
                              @RequestParam(required = false) Integer correctOptionIndex,
                              @RequestParam(required = false) List<Integer> correctOptionIndices,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(quizId, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to add questions to this quiz.");
            return "redirect:/quiz/list";
        }
        Quiz quiz = quizService.getQuizById(quizId);
        if (quiz.getStatus() == QuizStatus.STARTED) {
             redirectAttributes.addFlashAttribute("error", "Cannot add questions to a started quiz. Stop it first.");
             return "redirect:/quiz/" + quizId + "/edit";
        }
        
        question.setQuiz(quiz);

        if (questionImage != null && !questionImage.isEmpty()) {
            Attachment attachment = attachmentService.saveAttachment(questionImage);
            question.setAttachment(attachment);
        }

        Question savedQuestion = questionService.saveQuestion(question);

        List<AnswerOption> answerOptions = new ArrayList<>();
        for (int i = 0; i < optionTexts.size(); i++) {
            String text = optionTexts.get(i);
            MultipartFile image = (optionImages != null && optionImages.size() > i) ? optionImages.get(i) : null;

            boolean isTextEmpty = (text == null || text.trim().isEmpty());
            boolean isImageEmpty = (image == null || image.isEmpty());

            // Skip if both text and image are empty
            if (isTextEmpty && isImageEmpty) {
                continue;
            }

            Attachment optionAttachment = null;
            if (!isImageEmpty) {
                optionAttachment = attachmentService.saveAttachment(image);
            }

            boolean isCorrect = determineIsCorrect(question.getQuestionType(), i, correctOptionIndex, correctOptionIndices);
            AnswerOption option = new AnswerOption(savedQuestion, text, optionAttachment, isCorrect, i);
            answerOptions.add(option);
        }

        // Validation based on question type
        if (question.getQuestionType() != QuestionType.FILL_IN_THE_BLANK && answerOptions.size() < 2) {
            questionService.deleteQuestion(savedQuestion.getQuestionId()); // Rollback
            redirectAttributes.addFlashAttribute("error", "A question must have at least 2 options.");
            return "redirect:/quiz/" + quizId + "/edit"; // Redirect back to edit page
        }

        answerOptionService.saveAll(answerOptions);

        redirectAttributes.addFlashAttribute("success", "Question added successfully!");
        return "redirect:/quiz/" + quizId + "/edit";
    }

    // Edit Question in Quiz
    @GetMapping("/{quizId}/questions/{questionId}/edit")
    public String showEditQuestionForm(@PathVariable Long quizId, @PathVariable Long questionId, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(quizId, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit questions for this quiz.");
            return "redirect:/quiz/list";
        }
        Question question = questionService.getQuestionById(questionId);
        if (question == null || !question.getQuiz().getId().equals(quizId)) {
            redirectAttributes.addFlashAttribute("error", "Question not found or does not belong to this quiz.");
            return "redirect:/quiz/" + quizId + "/edit";
        }
        
        if (question.getQuiz().getStatus() == QuizStatus.STARTED) {
             redirectAttributes.addFlashAttribute("error", "Cannot edit questions of a started quiz. Stop it first.");
             return "redirect:/quiz/" + quizId + "/edit";
        }

        List<AnswerOption> answerOptions = answerOptionService.findActiveByQuestionId(questionId);
        model.addAttribute("question", question);
        model.addAttribute("answerOptions", answerOptions);
        model.addAttribute("quiz", question.getQuiz());
        model.addAttribute("categories", questionService.getAllCategories());
        
        // Pass correct indices to the view
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE_MULTI) {
            List<Integer> correctIndices = answerOptions.stream()
                .filter(AnswerOption::getIsCorrect)
                .map(AnswerOption::getOptionOrder)
                .toList();
            model.addAttribute("correctOptionIndices", correctIndices);
        } else {
            Integer correctIndex = answerOptions.stream()
                .filter(AnswerOption::getIsCorrect)
                .findFirst()
                .map(AnswerOption::getOptionOrder)
                .orElse(0);
            model.addAttribute("correctOptionIndex", correctIndex);
        }
        
        return "edit-question-for-quiz";
    }

    @PostMapping("/{quizId}/questions/{questionId}/edit")
    public String editQuestion(@PathVariable Long quizId, @PathVariable Long questionId, @ModelAttribute Question question,
                               @RequestParam List<String> optionTexts,
                               @RequestParam(value = "optionIds", required = false) List<Long> optionIds,
                               @RequestParam(value = "questionImage", required = false) MultipartFile questionImage,
                               @RequestParam(value = "removeQuestionImage", required = false) Boolean removeQuestionImage,
                               @RequestParam(value = "optionImages", required = false) List<MultipartFile> optionImages,
                               @RequestParam(value = "removeOptionImages", required = false) List<Boolean> removeOptionImages,
                               @RequestParam(required = false) Integer correctOptionIndex,
                               @RequestParam(required = false) List<Integer> correctOptionIndices,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(quizId, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to edit this quiz.");
            return "redirect:/quiz/list";
        }
        
        Question existingQuestion = questionService.getQuestionById(questionId);
        if (existingQuestion.getQuiz().getStatus() == QuizStatus.STARTED) {
             redirectAttributes.addFlashAttribute("error", "Cannot edit questions of a started quiz. Stop it first.");
             return "redirect:/quiz/" + quizId + "/edit";
        }
        
        existingQuestion.setQuestionText(question.getQuestionText());
        existingQuestion.setQuestionCategory(question.getQuestionCategory());
        existingQuestion.setQuestionType(question.getQuestionType());
        existingQuestion.setPoints(question.getPoints());
        existingQuestion.setTimeLimitSeconds(question.getTimeLimitSeconds());

        if (Boolean.TRUE.equals(removeQuestionImage)) {
            existingQuestion.setAttachment(null);
        }
        if (questionImage != null && !questionImage.isEmpty()) {
            Attachment attachment = attachmentService.saveAttachment(questionImage);
            existingQuestion.setAttachment(attachment);
        }

        List<AnswerOption> newOptions = new ArrayList<>();

        for (int i = 0; i < optionTexts.size(); i++) {
            String text = optionTexts.get(i);
            MultipartFile image = (optionImages != null && optionImages.size() > i) ? optionImages.get(i) : null;

            boolean isTextEmpty = (text == null || text.trim().isEmpty());
            boolean isImageEmpty = (image == null || image.isEmpty());

            // This logic is still simplified and doesn't handle existing attachments well,
            // but it implements the requested validation.
            if (isTextEmpty && isImageEmpty) {
                continue;
            }

            Attachment optionAttachment = null;
            if (!isImageEmpty) {
                optionAttachment = attachmentService.saveAttachment(image);
            }

            boolean isCorrect = determineIsCorrect(question.getQuestionType(), i, correctOptionIndex, correctOptionIndices);
            AnswerOption option = new AnswerOption(existingQuestion, text, optionAttachment, isCorrect, i);
            newOptions.add(option);
        }
        
        existingQuestion.getAnswerOptions().clear();
        existingQuestion.getAnswerOptions().addAll(newOptions);
        questionService.saveQuestion(existingQuestion);

        redirectAttributes.addFlashAttribute("success", "Question updated successfully!");
        return "redirect:/quiz/" + quizId + "/edit";
    }

    // Delete Question from Quiz
    @GetMapping("/{quizId}/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long quizId, @PathVariable Long questionId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(quizId, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to delete questions from this quiz.");
            return "redirect:/quiz/list";
        }
        Question question = questionService.getQuestionById(questionId);
        if (question.getQuiz().getStatus() == QuizStatus.STARTED) {
             redirectAttributes.addFlashAttribute("error", "Cannot delete questions from a started quiz. Stop it first.");
             return "redirect:/quiz/" + quizId + "/edit";
        }

        questionService.deleteQuestion(questionId);
        redirectAttributes.addFlashAttribute("success", "Question deleted successfully!");
        return "redirect:/quiz/" + quizId + "/edit";
    }

    // Helper for authorization
    private boolean isUserOwner(Long quizId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return false;
        Quiz quiz = quizService.getQuizById(quizId);
        return quiz != null && quiz.getUser().getUserId().equals(user.getUserId());
    }

    private boolean determineIsCorrect(QuestionType type, int currentIndex, Integer singleIndex, List<Integer> multiIndex) {
        return switch (type) {
            case MULTIPLE_CHOICE_MULTI -> multiIndex != null && multiIndex.contains(currentIndex);
            case FILL_IN_THE_BLANK -> true; // The single text field is always the correct answer
            default -> singleIndex != null && singleIndex == currentIndex;
        };
    }
}
