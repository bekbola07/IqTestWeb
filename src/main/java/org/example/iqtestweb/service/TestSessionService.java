package org.example.iqtestweb.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.repository.AnswerOptionRepository;
import org.example.iqtestweb.repository.QuestionRepository;
import org.example.iqtestweb.repository.TestSessionRepository;
import org.example.iqtestweb.repository.UserAnswerRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestSessionService {

    private final TestSessionRepository sessionRepository;

    private final UserAnswerRepository userAnswerRepository;

    private final QuestionRepository questionRepository;

    private final AnswerOptionRepository answerOptionRepository;

    @Transactional
    public TestSession saveSession(TestSession session) {
        return sessionRepository.save(session);
    }

//    @Transactional
//    public void submitAnswer(Long sessionId, Long questionId, Long optionId) {
//        TestSession session = sessionRepository.findById(sessionId).orElse(null);
//        Question question = questionRepository.findById(questionId).orElse(null);
//        AnswerOption selectedOption = answerOptionRepository.findById(optionId).orElse(null);
//
//        if (session != null && question != null && selectedOption != null) {
//            UserAnswer answer = new UserAnswer();
//            answer.setSession(session);
//            answer.setQuestion(question);
//            answer.setSelectedOption(selectedOption);
//            answer.setIsCorrect(selectedOption.getIsCorrect());
//            answer.setAnsweredAt(LocalDateTime.now());
//
//            userAnswerRepository.save(answer);
//        }
//    }

    @Transactional
    public TestSession completeSession(Long sessionId) {
        TestSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            session.setCompletedAt(LocalDateTime.now());

            List<UserAnswer> answers = userAnswerRepository.findBySessionSessionId(sessionId);
            long correctCount = answers.stream().filter(UserAnswer::getIsCorrect).count();

            session.setTotalQuestions(answers.size());
            session.setCorrectAnswers((int) correctCount);
            session.setIqScore(calculateIQ(correctCount, answers.size()));

            Duration duration = Duration.between(session.getStartedAt(), session.getCompletedAt());
            session.setTimeTakenSeconds((int) duration.getSeconds());

            return sessionRepository.save(session);
        }
        return null;
    }

    private int calculateIQ(long correct, int total) {
        if (total == 0) return 0;
        double percentage = (double) correct / total;
        return (int) (85 + (percentage * 30));
    }

    public TestSession getSession(Long sessionId) {
        return sessionRepository.findById(sessionId).orElse(null);
    }

    public List<TestSession> getUserSessions(Long userId) {
        return sessionRepository.findByUserUserId(userId);
    }

    public List<TestSession> getAllSessions() {
        return sessionRepository.findAllByOrderByCompletedAtDesc();
    }
}
