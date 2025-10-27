package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.AnswerOption;
import org.example.iqtestweb.entity.Question;
import org.example.iqtestweb.entity.TestSession;
import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.service.QuestionService;
import org.example.iqtestweb.service.TestSessionService;
import org.example.iqtestweb.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/test")
@RequiredArgsConstructor

public class TestController {
    private final QuestionService questionService;

    private final TestSessionService sessionService;

    private final UserService userService;

    @GetMapping("/start")
    public String startTest(HttpSession httpSession, Model model) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.findById(userId);
        TestSession session = sessionService.startSession(user);
        httpSession.setAttribute("sessionId", session.getSessionId());

        return "redirect:/test/question/0";
    }

    @GetMapping("/question/{index}")
    public String showQuestion(@PathVariable int index, HttpSession httpSession, Model model) {
        Long sessionId = (Long) httpSession.getAttribute("sessionId");
        if (sessionId == null) {
            return "redirect:/test/start";
        }

        List<Question> questions = questionService.getAllActiveQuestions();

        if (questions.isEmpty()) {
            model.addAttribute("error", "No active questions available");
            return "error";
        }

        if (index >= questions.size()) {
            return "redirect:/test/complete";
        }

        Question question = questions.get(index);
        List<AnswerOption> options = questionService.getOptionsForQuestion(question.getQuestionId());

        model.addAttribute("question", question);
        model.addAttribute("options", options);
        model.addAttribute("currentIndex", index);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("progress", ((index + 1) * 100) / questions.size());
        model.addAttribute("userName", httpSession.getAttribute("userName"));
        model.addAttribute("profilePicture", httpSession.getAttribute("profilePicture"));

        return "question";
    }

    @PostMapping("/submit-answer")
    public String submitAnswer(@RequestParam Long questionId,
                               @RequestParam Long optionId,
                               @RequestParam int nextIndex,
                               HttpSession httpSession) {
        Long sessionId = (Long) httpSession.getAttribute("sessionId");

        if (sessionId == null) {
            return "redirect:/test/start";
        }

        sessionService.submitAnswer(sessionId, questionId, optionId);
        return "redirect:/test/question/" + nextIndex;
    }

    @GetMapping("/complete")
    public String completeTest(HttpSession httpSession, Model model) {
        Long sessionId = (Long) httpSession.getAttribute("sessionId");

        if (sessionId == null) {
            return "redirect:/dashboard";
        }

        // Complete the session and calculate score
        TestSession completedSession = sessionService.completeSession(sessionId);

        if (completedSession == null) {
            model.addAttribute("error", "Session not found");
            return "redirect:/dashboard";
        }

        // Add all necessary data to model
        model.addAttribute("session", completedSession);
        model.addAttribute("userName", httpSession.getAttribute("userName"));
        model.addAttribute("profilePicture", httpSession.getAttribute("profilePicture"));
        model.addAttribute("userEmail", httpSession.getAttribute("userEmail"));

        // Calculate accuracy percentage
        if (completedSession.getTotalQuestions() != null && completedSession.getTotalQuestions() > 0) {
            double accuracy = (completedSession.getCorrectAnswers() * 100.0) / completedSession.getTotalQuestions();
            model.addAttribute("accuracy", Math.round(accuracy));
        } else {
            model.addAttribute("accuracy", 0);
        }

        // Add interpretation text based on score
        Integer iqScore = completedSession.getIqScore();
        if (iqScore != null) {
            String interpretation;
            String category;

            if (iqScore >= 130) {
                category = "Genius";
                interpretation = "Exceptional! Your score indicates very superior intelligence. You're in the top 2% of the population.";
            } else if (iqScore >= 120) {
                category = "Superior";
                interpretation = "Excellent! Your score indicates superior intelligence. You're in the top 10% of the population.";
            } else if (iqScore >= 110) {
                category = "High Average";
                interpretation = "Great! Your score indicates high average intelligence. You're performing above average.";
            } else if (iqScore >= 90) {
                category = "Average";
                interpretation = "Good! Your score indicates average intelligence. This is where most people score.";
            } else {
                category = "Below Average";
                interpretation = "Keep practicing! Intelligence can be improved with training and practice.";
            }

            model.addAttribute("category", category);
            model.addAttribute("iqScore", iqScore);
            model.addAttribute("interpretation", interpretation);
            model.addAttribute("correctAnswers", completedSession.getCorrectAnswers());
            model.addAttribute("totalQuestions", completedSession.getTotalQuestions());
            model.addAttribute("timeTaken", completedSession.getTimeTakenSeconds());
        }

        // Remove session from HttpSession
        httpSession.removeAttribute("sessionId");

        return "results";
    }

    @GetMapping("/history")
    public String testHistory(HttpSession httpSession, Model model) {
        Long userId = (Long) httpSession.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<TestSession> sessions = sessionService.getUserSessions(userId);
        model.addAttribute("sessions", sessions);
        model.addAttribute("userName", httpSession.getAttribute("userName"));
        model.addAttribute("profilePicture", httpSession.getAttribute("profilePicture"));

        return "test-history";
    }
}

