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
    private final AnswerOptionSnapshotService answerOptionSnapshotService;
    private final QuestionSnapshotService questionSnapshotService;
    private final QuizSnapshotService quizSnapshotService;
    private final CertificateService certificateService;

    @GetMapping("/select-quiz")
    public String selectQuizForTest(Model model, HttpSession session) {
        // Ensure user is in session for proper authorization checks later
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
        // If user is null in session, try to get it from SecurityContext
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
            return "redirect:/login"; // Redirect to login if user is still null
        }

        Quiz quiz = quizService.getQuizById(quizId);

        if (quiz == null) {
            redirectAttributes.addFlashAttribute("error", "Quiz not found.");
            return "redirect:/test/select-quiz";
        }

        if (quiz.getStatus() != QuizStatus.STARTED) {
            redirectAttributes.addFlashAttribute("error", "This quiz is not started yet. Please start it from the quiz management page.");
            return "redirect:/test/select-quiz"; // Redirect to quiz selection page
        }

        // Get or create QuizSnapshot
        QuizSnapshot quizSnapshot = quizSnapshotService.getOrCreateSnapshot(quiz);

        // Start or resume session
        TestSession testSession = testSessionService.startSession(user, quizSnapshot);

        session.setAttribute("testSession", testSession);
        
        // Find where the user left off if resuming
        if (testSession.getStatus() == Status.IN_PROGRESS) {
            List<UserAnswer> existingAnswers = userAnswerService.getUserAnswersBySessionId(testSession.getSessionId());
            int nextIndex = existingAnswers.size();
            return "redirect:/question/" + nextIndex;
        } else {
             return "redirect:/test/results/" + testSession.getSessionId(); // Updated redirect
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
        
        // Refresh session from DB to check status and time
        testSession = testSessionService.getSession(testSession.getSessionId());
        if (testSession.getStatus() != Status.IN_PROGRESS) {
             return "redirect:/test/results/" + testSession.getSessionId(); // Updated redirect
        }

        if (testSessionService.isSessionExpired(testSession)) {
            testSessionService.handleTimeout(testSession);
            return "redirect:/test/results/" + testSession.getSessionId(); // Updated redirect
        }

        // 🔥 Snapshot'lardan ma'lumot olish
        QuestionSnapshot questionSnapshot = questionSnapshotService.getById(questionSnapshotId);
        
        // Only process answer if option is selected
        if (optionSnapshotId != null) {
            AnswerOptionSnapshot selectedOptionSnapshot = answerOptionSnapshotService.getById(optionSnapshotId);

            // UserAnswer saqlash (endi snapshot'ga bog'lanadi)
            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setSession(testSession);
            userAnswer.setQuestionSnapshot(questionSnapshot);
            userAnswer.setSelectedOptionSnapshot(selectedOptionSnapshot);
            userAnswer.setIsCorrect(selectedOptionSnapshot.getIsCorrect());
            userAnswer.setTimeTakenSeconds(timeTakenSeconds); // Save time taken
            userAnswerService.saveUserAnswer(userAnswer);
        }

        // Keyingi savolga o'tish
        List<QuestionSnapshot> questionSnapshots = questionSnapshotService.findByQuizSnapshotId(testSession.getQuizSnapshot().getId());
        if (nextIndex < questionSnapshots.size()) {
            return "redirect:/question/" + nextIndex;
        } else {
            TestSessionService.TestSessionCompletionResult result = testSessionService.completeSession(testSession.getSessionId());
            if (result.isProfileDataRequired()) { // Changed from isAgeRequired()
                return "redirect:/profile"; // Redirect to the new user profile page
            }
            return "redirect:/test/results/" + testSession.getSessionId(); // Updated redirect
        }
    }

    // Removed the enterAge GET method
    // Removed the submitAge POST method

    @GetMapping("/results/{sessionId}") // Changed mapping
    public String showResults(@PathVariable Long sessionId, Model model, HttpSession session) { // Added @PathVariable
        TestSession testSession = testSessionService.getSession(sessionId); // Get session by ID
        if (testSession == null) {
            return "redirect:/dashboard"; // Redirect if session not found
        }
        
        // Refresh from DB to ensure we have latest status
        // testSession = testSessionService.getSession(testSession.getSessionId()); // Already fetched by ID

        if (testSession.getStatus() == Status.IN_PROGRESS) {
             // If user navigates to results manually, try to complete session
             TestSessionService.TestSessionCompletionResult result = testSessionService.completeSession(testSession.getSessionId());
             if (result.isProfileDataRequired()) { // Changed from isAgeRequired()
                 return "redirect:/profile"; // Redirect to the new user profile page
             }
             testSession = result.getSession();
        }

        List<UserAnswer> userAnswers = userAnswerService.getUserAnswersBySessionId(testSession.getSessionId());
        long correctAnswers = userAnswers.stream().filter(UserAnswer::getIsCorrect).count();
        
        // Use quiz snapshot to get total questions
        int totalQuestions = questionSnapshotService.findByQuizSnapshotId(testSession.getQuizSnapshot().getId()).size();
        
        int score = userAnswers.stream()
                .filter(UserAnswer::getIsCorrect)
                .mapToInt(i -> i.getQuestionSnapshot().getPoints())
                .sum();

        model.addAttribute("testSession", testSession);
        model.addAttribute("correctAnswers", correctAnswers);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("iqScore", testSession.getIqScore()); // Use calculated IQ score
        model.addAttribute("accuracy", totalQuestions > 0 ? (int) (((double) correctAnswers / totalQuestions) * 100) : 0);
        
        // Add certificate enabled flag and check if already generated
        boolean certificateEnabled = testSession.getQuizSnapshot().getQuiz().isCertificateEnabled();
        model.addAttribute("certificateEnabled", certificateEnabled);
        
        boolean certificateGenerated = false;
        if (certificateEnabled) {
            certificateGenerated = certificateService.getUserCertificateBySessionId(testSession.getSessionId()).isPresent();
        }
        model.addAttribute("certificateGenerated", certificateGenerated);

        session.removeAttribute("testSession"); // Clear session after showing results

        return "results";
    }
}
