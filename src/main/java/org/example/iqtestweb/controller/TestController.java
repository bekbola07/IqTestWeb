package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.entity.enums.QuizStatus;
import org.example.iqtestweb.entity.enums.Status;
import org.example.iqtestweb.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    private final AnswerOptionSnapshotService answerOptionSnapshotService;
    private final QuestionSnapshotService questionSnapshotService;
    private final QuizSnapshotService quizSnapshotService;
    private final CertificateService certificateService;

    @GetMapping("/select-quiz")
    public String selectQuizForTest(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
                user = userService.findByUsername(userDetails.getUsername());
                if (user != null) {
                    session.setAttribute("user", user);
                    session.setAttribute("userId", user.getUserId());
                    session.setAttribute("userName", user.getUsername());
                    session.setAttribute("userEmail", user.getEmail());
                    session.setAttribute("profilePicture", user.getProfilePictureUrl());
                    session.setAttribute("userRole", user.getRole().name());
                }
            }
        }

        List<Quiz> startedQuizzes = quizService.getAllQuizzes().stream()
                .filter(quiz -> quiz.getStatus() == QuizStatus.STARTED)
                .collect(Collectors.toList());
        model.addAttribute("quizzes", startedQuizzes);
        return "select-quiz-for-test";
    }

    @GetMapping("/start/{quizId}")
    @Transactional
    public String startTest(@PathVariable Long quizId, HttpSession session, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                user = userService.findByUsername(userDetails.getUsername());
                if (user != null) {
                    session.setAttribute("user", user);
                    session.setAttribute("userId", user.getUserId());
                    session.setAttribute("userName", user.getUsername());
                    session.setAttribute("userEmail", user.getEmail());
                    session.setAttribute("profilePicture", user.getProfilePictureUrl());
                    session.setAttribute("userRole", user.getRole().name());
                }
            }
        }

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not authenticated.");
            return "redirect:/login";
        }

        Quiz quiz = quizService.getQuizById(quizId);
        if (quiz == null) {
            redirectAttributes.addFlashAttribute("error", "Quiz not found.");
            return "redirect:/test/select-quiz";
        }

        if (quiz.getStatus() != QuizStatus.STARTED) {
            redirectAttributes.addFlashAttribute("error", "This quiz is not started yet.");
            return "redirect:/test/select-quiz";
        }

        QuizSnapshot quizSnapshot = quizSnapshotService.getOrCreateSnapshot(quiz);
        TestSession testSession = testSessionService.startSession(user, quizSnapshot);
        session.setAttribute("testSession", testSession);

        if (testSession.getStatus() == Status.IN_PROGRESS) {
            List<UserAnswer> existingAnswers = userAnswerService.getUserAnswersBySessionId(testSession.getSessionId());
            int nextIndex = existingAnswers.size();
            return "redirect:/question/" + nextIndex;
        } else {
            return "redirect:/test/results/" + testSession.getSessionId();
        }
    }

    @PostMapping("/submit-answer")
    public String submitAnswer(@RequestParam Long questionSnapshotId,
                               @RequestParam(required = false) Long optionSnapshotId,
                               @RequestParam int nextIndex,
                               @RequestParam(defaultValue = "0") int timeTakenSeconds,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        TestSession testSession = (TestSession) session.getAttribute("testSession");
        if (testSession == null) {
            redirectAttributes.addFlashAttribute("error", "Test session expired.");
            return "redirect:/test/select-quiz";
        }

        testSession = testSessionService.getSession(testSession.getSessionId());
        if (testSession.getStatus() != Status.IN_PROGRESS) {
            return "redirect:/test/results/" + testSession.getSessionId();
        }

        if (testSessionService.isSessionExpired(testSession)) {
            testSessionService.handleTimeout(testSession);
            return "redirect:/test/results/" + testSession.getSessionId();
        }

        QuestionSnapshot questionSnapshot = questionSnapshotService.getById(questionSnapshotId);

        if (optionSnapshotId != null) {
            AnswerOptionSnapshot selectedOptionSnapshot = answerOptionSnapshotService.getById(optionSnapshotId);
            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setSession(testSession);
            userAnswer.setQuestionSnapshot(questionSnapshot);
            userAnswer.setSelectedOptionSnapshot(selectedOptionSnapshot);
            userAnswer.setIsCorrect(selectedOptionSnapshot.getIsCorrect());
            userAnswer.setTimeTakenSeconds(timeTakenSeconds);
            userAnswerService.saveUserAnswer(userAnswer);
        }

        List<QuestionSnapshot> questionSnapshots = questionSnapshotService.findByQuizSnapshotId(testSession.getQuizSnapshot().getId());
        if (nextIndex < questionSnapshots.size()) {
            return "redirect:/question/" + nextIndex;
        } else {
            // ✅ sessionId ni oldindan saqlab qo'yamiz
            Long sessionId = testSession.getSessionId();
            TestSessionService.TestSessionCompletionResult result = testSessionService.completeSession(sessionId);

            if (result.isProfileDataRequired()) {
                // ✅ sessionId ni profile sahifasiga uzatamiz
                return "redirect:/profile?sessionId=" + sessionId;
            }
            return "redirect:/test/results/" + sessionId;
        }
    }

    @GetMapping("/results/{sessionId}")
    public String showResults(@PathVariable Long sessionId, Model model, HttpSession session) {
        TestSession testSession = testSessionService.getSession(sessionId);
        if (testSession == null) {
            return "redirect:/dashboard";
        }

        if (testSession.getStatus() == Status.IN_PROGRESS) {
            TestSessionService.TestSessionCompletionResult result = testSessionService.completeSession(testSession.getSessionId());
            if (result.isProfileDataRequired()) {
                // ✅ Bu yerda ham sessionId uzatiladi
                return "redirect:/profile?sessionId=" + sessionId;
            }
            testSession = result.getSession();
        }

        List<UserAnswer> userAnswers = userAnswerService.getUserAnswersBySessionId(testSession.getSessionId());
        long correctAnswers = userAnswers.stream().filter(UserAnswer::getIsCorrect).count();
        int totalQuestions = questionSnapshotService.findByQuizSnapshotId(testSession.getQuizSnapshot().getId()).size();

        int score = userAnswers.stream()
                .filter(UserAnswer::getIsCorrect)
                .mapToInt(i -> i.getQuestionSnapshot().getPoints())
                .sum();

        model.addAttribute("testSession", testSession);
        model.addAttribute("correctAnswers", correctAnswers);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("iqScore", testSession.getIqScore());
        model.addAttribute("accuracy", totalQuestions > 0 ? (int) (((double) correctAnswers / totalQuestions) * 100) : 0);

        boolean certificateEnabled = testSession.getQuizSnapshot().getQuiz().isCertificateEnabled();
        model.addAttribute("certificateEnabled", certificateEnabled);

        boolean certificateGenerated = false;
        if (certificateEnabled) {
            certificateGenerated = certificateService.getUserCertificateBySessionId(testSession.getSessionId()).isPresent();
        }
        model.addAttribute("certificateGenerated", certificateGenerated);

        session.removeAttribute("testSession");
        return "results";
    }
}