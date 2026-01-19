package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.entity.enums.QuizStatus;
import org.example.iqtestweb.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
        return "create-quiz";
    }

    @PostMapping("/create")
    public String createQuiz(@ModelAttribute Quiz quiz, @RequestParam(value = "file", required = false) MultipartFile file, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        quiz.setUser(user);
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
    public String editQuiz(@PathVariable Long id, @ModelAttribute Quiz quiz, 
                           @RequestParam(value = "file", required = false) MultipartFile file, 
                           @RequestParam(value = "removeImage", required = false) Boolean removeImage,
                           HttpSession session, RedirectAttributes redirectAttributes) {
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
        dbQuiz.setTimeType(quiz.getTimeType());
        dbQuiz.setTimeLimitSeconds(quiz.getTimeLimitSeconds());
        
        if (Boolean.TRUE.equals(removeImage)) {
            dbQuiz.setAttachment(null);
        }

        if (file != null && !file.isEmpty()) {
            Attachment attachment = attachmentService.saveAttachment(file);
            dbQuiz.setAttachment(attachment);
        }
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
                              @RequestParam List<String> optionTexts,
                              @RequestParam(value = "questionImage", required = false) MultipartFile questionImage,
                              @RequestParam(value = "questionWebUrl", required = false) String questionWebUrl,
                              @RequestParam(value = "optionImages", required = false) List<MultipartFile> optionImages,
                              @RequestParam(value = "optionWebUrls", required = false) List<String> optionWebUrls,
                              @RequestParam int correctOptionIndex, HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isUserOwner(quizId, session)) {
            redirectAttributes.addFlashAttribute("error", "You are not authorized to add questions to this quiz.");
            return "redirect:/quiz/list";
        }
        Quiz quiz = quizService.getQuizById(quizId);
        question.setQuiz(quiz);

        // Handle Question Attachment
        if (questionImage != null && !questionImage.isEmpty()) {
            Attachment attachment = attachmentService.saveAttachment(questionImage);
            question.setAttachment(attachment);
        } else if (questionWebUrl != null && !questionWebUrl.trim().isEmpty()) {
            Attachment attachment = attachmentService.saveWebAttachment(questionWebUrl);
            question.setAttachment(attachment);
        }

        Question savedQuestion = questionService.saveQuestion(question);

        List<AnswerOption> answerOptions = new ArrayList<>();
        for (int i = 0; i < optionTexts.size(); i++) {
            String text = optionTexts.get(i);
            Attachment optionAttachment = null;

            // Handle Option Attachment
            if (optionImages != null && optionImages.size() > i && !optionImages.get(i).isEmpty()) {
                optionAttachment = attachmentService.saveAttachment(optionImages.get(i));
            } else if (optionWebUrls != null && optionWebUrls.size() > i && optionWebUrls.get(i) != null && !optionWebUrls.get(i).trim().isEmpty()) {
                optionAttachment = attachmentService.saveWebAttachment(optionWebUrls.get(i));
            }

            // Validation: Either text or attachment must be present
            if ((text == null || text.trim().isEmpty()) && optionAttachment == null) {
                // Skip empty options or handle error. For now, let's skip but ensure at least 2 options exist logic elsewhere
                continue; 
            }

            AnswerOption option = new AnswerOption(savedQuestion, text, optionAttachment, i == correctOptionIndex, i);
            answerOptions.add(option);
        }
        
        if (answerOptions.size() < 2) {
             redirectAttributes.addFlashAttribute("error", "A question must have at least 2 valid options (text or image).");
             // Ideally rollback or delete saved question, but for simplicity redirecting back
             questionService.deleteQuestion(savedQuestion.getQuestionId());
             return "redirect:/quiz/" + quizId + "/questions/add";
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
        model.addAttribute("correctOptionIndex", correctOptionIndex);
        return "edit-question-for-quiz";
    }

    @PostMapping("/{quizId}/questions/{questionId}/edit")
    public String editQuestion(@PathVariable Long quizId, @PathVariable Long questionId, @ModelAttribute Question question,
                               @RequestParam List<String> optionTexts,
                               @RequestParam(value = "questionImage", required = false) MultipartFile questionImage,
                               @RequestParam(value = "questionWebUrl", required = false) String questionWebUrl,
                               @RequestParam(value = "removeQuestionImage", required = false) Boolean removeQuestionImage,
                               @RequestParam(value = "optionImages", required = false) List<MultipartFile> optionImages,
                               @RequestParam(value = "optionWebUrls", required = false) List<String> optionWebUrls,
                               @RequestParam(value = "removeOptionImages", required = false) List<Boolean> removeOptionImages,
                               @RequestParam(value = "existingOptionAttachmentIds", required = false) List<Long> existingOptionAttachmentIds,
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
        
        // Handle Question Attachment Removal
        if (Boolean.TRUE.equals(removeQuestionImage)) {
            existingQuestion.setAttachment(null);
        }

        // Handle Question Attachment Update
        if (questionImage != null && !questionImage.isEmpty()) {
            Attachment attachment = attachmentService.saveAttachment(questionImage);
            existingQuestion.setAttachment(attachment);
        } else if (questionWebUrl != null && !questionWebUrl.trim().isEmpty()) {
            Attachment attachment = attachmentService.saveWebAttachment(questionWebUrl);
            existingQuestion.setAttachment(attachment);
        }
        
        existingQuestion.setQuestionType(question.getQuestionType());
        existingQuestion.setDifficultyLevel(question.getDifficultyLevel());
        existingQuestion.setTimeLimitSeconds(question.getTimeLimitSeconds());
        existingQuestion.setPoints(question.getPoints());
        
        // Clear existing options
        existingQuestion.getAnswerOptions().clear();
        
        // Add new options
        List<AnswerOption> newOptions = new ArrayList<>();
        for (int i = 0; i < optionTexts.size(); i++) {
            String text = optionTexts.get(i);
            Attachment optionAttachment = null;
            
            // Check if removal requested for this option
            boolean removeImage = false;
            if (removeOptionImages != null && removeOptionImages.size() > i && removeOptionImages.get(i) != null) {
                removeImage = removeOptionImages.get(i);
            }

            // Check for new file upload
            if (optionImages != null && optionImages.size() > i && !optionImages.get(i).isEmpty()) {
                optionAttachment = attachmentService.saveAttachment(optionImages.get(i));
            } 
            // Check for new Web URL
            else if (optionWebUrls != null && optionWebUrls.size() > i && optionWebUrls.get(i) != null && !optionWebUrls.get(i).trim().isEmpty()) {
                optionAttachment = attachmentService.saveWebAttachment(optionWebUrls.get(i));
            }
            // Check for existing attachment (if not removed)
            else if (!removeImage && existingOptionAttachmentIds != null && existingOptionAttachmentIds.size() > i && existingOptionAttachmentIds.get(i) != null) {
                try {
                    optionAttachment = attachmentService.getAttachment(existingOptionAttachmentIds.get(i));
                } catch (Exception e) {
                    // Ignore
                }
            }

            // Validation
            if ((text == null || text.trim().isEmpty()) && optionAttachment == null) {
                continue;
            }

            AnswerOption option = new AnswerOption(existingQuestion, text, optionAttachment, i == correctOptionIndex, i);
            newOptions.add(option);
        }
        
        if (newOptions.size() < 2) {
             redirectAttributes.addFlashAttribute("error", "A question must have at least 2 valid options (text or image).");
             return "redirect:/quiz/" + quizId + "/questions/" + questionId + "/edit";
        }
        
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
