package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.entity.enums.QuizTimeType;
import org.example.iqtestweb.entity.enums.Status;
import org.example.iqtestweb.service.AnswerOptionSnapshotService;
import org.example.iqtestweb.service.QuestionSnapshotService;
import org.example.iqtestweb.service.TestSessionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionSnapshotService questionSnapshotService;
    private final AnswerOptionSnapshotService answerOptionSnapshotService;
    private final TestSessionService testSessionService;

    @GetMapping("/{index}")
    public String showQuestion(@PathVariable int index, HttpSession session, Model model) {
        TestSession testSession = (TestSession) session.getAttribute("testSession");
        if (testSession == null) {
            return "redirect:/test/select-quiz"; // No active test session
        }
        
        // Refresh session from DB
        testSession = testSessionService.getSession(testSession.getSessionId());
        if (testSession.getStatus() != Status.IN_PROGRESS) {
            return "redirect:/test/results";
        }

        if (testSessionService.isSessionExpired(testSession)) {
            testSessionService.handleTimeout(testSession);
            return "redirect:/test/results";
        }

        QuizSnapshot quizSnapshot = testSession.getQuizSnapshot();
        if (quizSnapshot == null) {
            return "redirect:/test/select-quiz"; // Quiz snapshot not found in session
        }

        List<QuestionSnapshot> questionSnapshots = questionSnapshotService.findByQuizSnapshotId(quizSnapshot.getId());
        if (questionSnapshots == null || questionSnapshots.isEmpty()) {
            return "redirect:/test/select-quiz"; // No questions in the quiz
        }

        if (index >= 0 && index < questionSnapshots.size()) {
            QuestionSnapshot currentQuestionSnapshot = questionSnapshots.get(index);
            List<AnswerOptionSnapshot> optionSnapshots = answerOptionSnapshotService.findByQuestionSnapshotIdOrderByOptionOrder(currentQuestionSnapshot.getSnapshotId());

            model.addAttribute("questionSnapshot", currentQuestionSnapshot);
            model.addAttribute("optionSnapshots", optionSnapshots);
            model.addAttribute("currentIndex", index);
            model.addAttribute("totalQuestions", questionSnapshots.size());
            int progress = (int) (((double) (index + 1) / questionSnapshots.size()) * 100);
            model.addAttribute("progress", progress);
            model.addAttribute("userName", session.getAttribute("userName"));
            model.addAttribute("profilePicture", session.getAttribute("profilePicture"));

            // Time logic
            if (quizSnapshot.getTimeType() == QuizTimeType.PER_QUESTION) {
                model.addAttribute("timeLimitSeconds", currentQuestionSnapshot.getTimeLimitSeconds());
                model.addAttribute("timeType", "PER_QUESTION");
            } else if (quizSnapshot.getTimeType() == QuizTimeType.TOTAL_TIME) {
                if (testSession.getExpiresAt() != null) {
                    long remainingSeconds = Duration.between(LocalDateTime.now(), testSession.getExpiresAt()).getSeconds();
                    model.addAttribute("timeLimitSeconds", Math.max(0, remainingSeconds));
                    model.addAttribute("timeType", "TOTAL_TIME");
                }
            }

            return "question";
        } else if (index == questionSnapshots.size()) {
            return "redirect:/test/results";
        } else {
            return "redirect:/test/select-quiz";
        }
    }
}
