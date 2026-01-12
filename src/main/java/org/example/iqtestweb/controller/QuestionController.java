package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.service.AnswerOptionService;
import org.example.iqtestweb.service.AnswerOptionSnapshotService;
import org.example.iqtestweb.service.QuestionService;
import org.example.iqtestweb.service.QuestionSnapshotService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final AnswerOptionService answerOptionService;
    private final QuestionSnapshotService questionSnapshotService;
    private final AnswerOptionSnapshotService answerOptionSnapshotService;

    @GetMapping("/{index}")
    public String showQuestion(@PathVariable int index, HttpSession session, Model model) {
        TestSession testSession = (TestSession) session.getAttribute("testSession");
        if (testSession == null) {
            return "redirect:/test/select-quiz"; // No active test session
        }

        Quiz quiz = testSession.getQuiz();
        if (quiz == null) {
            return "redirect:/test/select-quiz"; // Quiz not found in session
        }

        List<Question> questions = questionService.getQuestionsByQuizId(quiz.getId());
        if (questions == null || questions.isEmpty()) {
            return "redirect:/test/select-quiz"; // No questions in the quiz
        }

        if (index >= 0 && index < questions.size()) {
            Question currentQuestion = questions.get(index);
            QuestionSnapshot lastQuestionSnapshot = questionSnapshotService.findByOriginalQuestionId(currentQuestion.getQuestionId()).getLast();

            List<AnswerOptionSnapshot> optionSnapshots = answerOptionSnapshotService.findByQuestionSnapshotId(lastQuestionSnapshot.getOriginalQuestionId());
            List<AnswerOption> answerOptions = answerOptionService.findByQuestionId(currentQuestion.getQuestionId());

            model.addAttribute("questionSnapshot", lastQuestionSnapshot);
            model.addAttribute("optionSnapshots", optionSnapshots);
            model.addAttribute("currentIndex", index);
            model.addAttribute("totalQuestions", questions.size());
            int progress = (int) (((double) (index + 1) / questions.size()) * 100);
            model.addAttribute("progress", progress);
            model.addAttribute("userName", session.getAttribute("userName"));
            model.addAttribute("profilePicture", session.getAttribute("profilePicture"));
            return "question";
        } else if (index == questions.size()) {
            return "redirect:/results";
        } else {
            return "redirect:/test/select-quiz";
        }
    }
}
