package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.entity.enums.QuizStatus;
import org.example.iqtestweb.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final QuizService quizService;
    private final TestSessionService testSessionService;
    private final UserService userService;
    private final UserAnswerService userAnswerService;
    private final AnswerOptionService answerOptionService;
    private final QuestionService questionService;

    @GetMapping("/select-quiz")
    public String selectQuizForTest(Model model, HttpSession session) {
        // ... (existing code)
        List<Quiz> startedQuizzes = quizService.getAllQuizzes().stream()
                .filter(quiz -> quiz.getStatus() == QuizStatus.STARTED)
                .collect(Collectors.toList());
        model.addAttribute("quizzes", startedQuizzes);
        return "select-quiz-for-test";
    }

    @GetMapping("/start/{quizId}")
    public String startTest(@PathVariable Long quizId, HttpSession session, RedirectAttributes redirectAttributes) {
        // ... (existing code)
        User user = (User) session.getAttribute("user");
        if (user == null) {
            // ... (logic to get user from SecurityContext)
        }
        if (user == null) {
            return "redirect:/login";
        }

        Quiz quiz = quizService.getQuizById(quizId);
        if (quiz == null || quiz.getStatus() != QuizStatus.STARTED) {
            // ... (error handling)
            return "redirect:/test/select-quiz";
        }

        TestSession testSession = new TestSession();
        testSession.setUser(user);
        testSession.setQuiz(quiz);
        testSession.setStartedAt(LocalDateTime.now());
        testSession = testSessionService.saveSession(testSession);

        session.setAttribute("testSession", testSession);
        return "redirect:/question/0";
    }

    @PostMapping("/submit-answer")
    public String submitAnswer(@RequestParam Long questionId,
                               @RequestParam Long optionId,
                               @RequestParam int nextIndex,
                               HttpSession session) {
        TestSession testSession = (TestSession) session.getAttribute("testSession");
        AnswerOption selectedOption = answerOptionService.getAnswerOptionById(optionId);

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setSession(testSession);
        userAnswer.setQuestion(questionService.getQuestionById(questionId));
        userAnswer.setSelectedOption(selectedOption);
        userAnswer.setIsCorrect(selectedOption.getIsCorrect());
        userAnswerService.saveUserAnswer(userAnswer);

        List<Question> questions = questionService.getQuestionsByQuizId(testSession.getQuiz().getId());
        if (nextIndex < questions.size()) {
            return "redirect:/question/" + nextIndex;
        } else {
            return "redirect:/test/results";
        }
    }

    @GetMapping("/results")
    public String showResults(Model model, HttpSession session) {
        TestSession testSession = (TestSession) session.getAttribute("testSession");
        if (testSession == null) {
            return "redirect:/dashboard";
        }

        List<UserAnswer> userAnswers = userAnswerService.getUserAnswersBySessionId(testSession.getSessionId());
        long correctAnswers = userAnswers.stream().filter(UserAnswer::getIsCorrect).count();
        int totalQuestions = questionService.getQuestionsByQuizId(testSession.getQuiz().getId()).size();
        
        // Simple IQ calculation logic (can be improved)
        int iqScore = 80 + (int) Math.round(((double) correctAnswers / totalQuestions) * 60);

        testSession.setCompletedAt(LocalDateTime.now());
        testSession.setCorrectAnswers((int) correctAnswers);
        testSession.setTotalQuestions(totalQuestions);
        testSession.setIqScore(iqScore);
        
        long timeTaken = Duration.between(testSession.getStartedAt(), testSession.getCompletedAt()).getSeconds();
        testSession.setTimeTakenSeconds((int) timeTaken);

        testSessionService.saveSession(testSession);

        model.addAttribute("testSession", testSession);
        model.addAttribute("correctAnswers", correctAnswers);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("iqScore", iqScore);

        session.removeAttribute("testSession"); // Clear session after showing results

        return "results";
    }
}
