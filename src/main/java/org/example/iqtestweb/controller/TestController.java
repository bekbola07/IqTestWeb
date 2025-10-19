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
class TestController {
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
        sessionService.submitAnswer(sessionId, questionId, optionId);
        return "redirect:/test/question/" + nextIndex;
    }

    @GetMapping("/complete")
    public String completeTest(HttpSession httpSession, Model model) {
        Long sessionId = (Long) httpSession.getAttribute("sessionId");
        TestSession completedSession = sessionService.completeSession(sessionId);

        model.addAttribute("session", completedSession);
        model.addAttribute("userName", httpSession.getAttribute("userName"));
        model.addAttribute("profilePicture", httpSession.getAttribute("profilePicture"));

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

        return "test-history";
    }
}
