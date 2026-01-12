package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
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
    private final AnswerOptionSnapshotService answerOptionSnapshotService;
    private final QuestionService questionService;
    private final QuestionSnapshotService questionSnapshotService;

    @GetMapping("/select-quiz")
    public String selectQuizForTest(Model model, HttpSession session) {
        // ... (existing code)
        List<Quiz> startedQuizzes = quizService.getAllQuizzes().stream()
                .filter(quiz -> quiz.getStatus() == QuizStatus.STARTED)
                .collect(Collectors.toList());
        model.addAttribute("quizzes", startedQuizzes);
        return "select-quiz-for-test";
    }

//    @GetMapping("/start/{quizId}")
//    public String startTest(@PathVariable Long quizId, HttpSession session, RedirectAttributes redirectAttributes) {
//        // ... (existing code)
//        User user = (User) session.getAttribute("user");
//        if (user == null) {
//            // ... (logic to get user from SecurityContext)
//        }
//        if (user == null) {
//            return "redirect:/login";
//        }
//
//        Quiz quiz = quizService.getQuizById(quizId);
//        if (quiz == null || quiz.getStatus() != QuizStatus.STARTED) {
//            // ... (error handling)
//            return "redirect:/test/select-quiz";
//        }
//
//        TestSession testSession = new TestSession();
//        testSession.setUser(user);
//        testSession.setQuiz(quiz);
//        testSession.setStartedAt(LocalDateTime.now());
//        testSession = testSessionService.saveSession(testSession);
//
//        session.setAttribute("testSession", testSession);
//        return "redirect:/question/0";
//    }
    // TestController.java - startTest metodida
    @GetMapping("/start/{quizId}")
    @Transactional
    public String startTest(@PathVariable Long quizId, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        Quiz quiz = quizService.getQuizById(quizId);

        // Test session yaratish
        TestSession testSession = new TestSession();
        testSession.setUser(user);
        testSession.setQuiz(quiz);
        testSession.setStartedAt(LocalDateTime.now());
        testSession = testSessionService.saveSession(testSession);

        // 🔥 SNAPSHOT YARATISH - asosiy qism!
        List<Question> questions = questionService.getQuestionsByQuizId(quizId);
        for (Question question : questions) {
            // Question snapshot
            QuestionSnapshot qSnapshot = new QuestionSnapshot();
            qSnapshot.setTestSession(testSession);
            qSnapshot.setQuestionText(question.getQuestionText());
            qSnapshot.setQuestionImageUrl(question.getQuestionImageUrl());
            qSnapshot.setDifficultyLevel(question.getDifficultyLevel());
            qSnapshot.setPoints(question.getPoints());
            qSnapshot.setOriginalQuestionId(question.getQuestionId());
            qSnapshot.setQuestionType(question.getQuestionType());
            qSnapshot.setTimeLimitSeconds(question.getTimeLimitSeconds());
            qSnapshot = questionSnapshotService.save(qSnapshot);

            // Answer options snapshot
            List<AnswerOption> options = answerOptionService.findByQuestionId(question.getQuestionId());
            for (AnswerOption option : options) {
                AnswerOptionSnapshot optSnapshot = new AnswerOptionSnapshot();
                optSnapshot.setQuestionSnapshot(qSnapshot);
                optSnapshot.setOptionText(option.getOptionText());
                optSnapshot.setImageUrl(option.getImageUrl());
                optSnapshot.setIsCorrect(option.getIsCorrect());
                optSnapshot.setOptionOrder(option.getOptionOrder());
                optSnapshot.setOriginalOptionId(option.getOptionId());
                answerOptionSnapshotService.save(optSnapshot);
            }
        }

        session.setAttribute("testSession", testSession);
        return "redirect:/question/0";
    }

//    @PostMapping("/submit-answer")
//    public String submitAnswer(@RequestParam Long questionId,
//                               @RequestParam Long optionId,
//                               @RequestParam int nextIndex,
//                               HttpSession session) {
//        TestSession testSession = (TestSession) session.getAttribute("testSession");
//        AnswerOption selectedOption = answerOptionService.getAnswerOptionById(optionId);
//
//        UserAnswer userAnswer = new UserAnswer();
//        userAnswer.setSession(testSession);
//        userAnswer.setQuestion(questionService.getQuestionById(questionId));
//        userAnswer.setSelectedOption(selectedOption);
//        userAnswer.setIsCorrect(selectedOption.getIsCorrect());
//        userAnswerService.saveUserAnswer(userAnswer);
//
//        List<Question> questions = questionService.getQuestionsByQuizId(testSession.getQuiz().getId());
//        if (nextIndex < questions.size()) {
//            return "redirect:/question/" + nextIndex;
//        } else {
//            return "redirect:/test/results";
//        }
//    }
@PostMapping("/submit-answer")
public String submitAnswer(@RequestParam Long questionSnapshotId,
                           @RequestParam Long optionSnapshotId,
                           @RequestParam int nextIndex,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
    TestSession testSession = (TestSession) session.getAttribute("testSession");
    if (testSession == null) {
        redirectAttributes.addFlashAttribute("error", "Test session expired.");
        return "redirect:/test/select-quiz";
    }

    // 🔥 Snapshot'lardan ma'lumot olish
    QuestionSnapshot questionSnapshot = questionSnapshotService.getById(questionSnapshotId);
    AnswerOptionSnapshot selectedOptionSnapshot = answerOptionSnapshotService.getById(optionSnapshotId);

    // UserAnswer saqlash (endi snapshot'ga bog'lanadi)
    UserAnswer userAnswer = new UserAnswer();
    userAnswer.setSession(testSession);
    userAnswer.setQuestionSnapshot(questionSnapshot);
    userAnswer.setSelectedOptionSnapshot(selectedOptionSnapshot);
    userAnswer.setIsCorrect(selectedOptionSnapshot.getIsCorrect());
    userAnswerService.saveUserAnswer(userAnswer);

    // Keyingi savolga o'tish
    List<QuestionSnapshot> questionSnapshots = questionSnapshotService.findByTestSessionId(testSession.getSessionId());
    if (nextIndex < questionSnapshots.size()) {
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
//        int iqScore = 80 + (int) Math.round(((double) correctAnswers / totalQuestions) * 60);

        int score = userAnswers.stream().mapToInt(i -> i.getQuestionSnapshot().getPoints()).sum();

        testSession.setCompletedAt(LocalDateTime.now());
        testSession.setCorrectAnswers((int) correctAnswers);
        testSession.setTotalQuestions(totalQuestions);
//        testSession.setIqScore(iqScore);
        testSession.setIqScore(score);

        long timeTaken = Duration.between(testSession.getStartedAt(), testSession.getCompletedAt()).getSeconds();
        testSession.setTimeTakenSeconds((int) timeTaken);

        testSessionService.saveSession(testSession);

        model.addAttribute("testSession", testSession);
        model.addAttribute("correctAnswers", correctAnswers);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("iqScore", score);

        session.removeAttribute("testSession"); // Clear session after showing results

        return "results";
    }
}
