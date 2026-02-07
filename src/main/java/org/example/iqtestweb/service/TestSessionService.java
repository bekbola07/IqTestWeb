package org.example.iqtestweb.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.entity.enums.QuizTimeType;
import org.example.iqtestweb.entity.enums.Status;
import org.example.iqtestweb.repository.TestSessionRepository;
import org.example.iqtestweb.repository.UserAnswerRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestSessionService {

    private final TestSessionRepository sessionRepository;

    private final UserAnswerRepository userAnswerRepository;
    
    private final CertificateService certificateService;

    @Transactional
    public TestSession startSession(User user, QuizSnapshot quizSnapshot) {
        // Check for existing active session for this quiz
        Optional<TestSession> existingSession = sessionRepository.findByUserUserId(user.getUserId()).stream()
                .filter(s -> s.getQuizSnapshot().getId().equals(quizSnapshot.getId()) && s.getStatus() == Status.IN_PROGRESS)
                .findFirst();

        if (existingSession.isPresent()) {
            TestSession session = existingSession.get();
            if (isSessionExpired(session)) {
                handleTimeout(session);
            } else {
                return session;
            }
        }

        TestSession session = new TestSession();
        session.setUser(user);
        session.setQuizSnapshot(quizSnapshot);
        session.setStartedAt(LocalDateTime.now());
        session.setStatus(Status.IN_PROGRESS);

        if (quizSnapshot.getTimeType() == QuizTimeType.TOTAL_TIME && quizSnapshot.getTimeLimitSeconds() != null) {
            session.setExpiresAt(session.getStartedAt().plusSeconds(quizSnapshot.getTimeLimitSeconds()));
        }

        return sessionRepository.save(session);
    }

    @Transactional
    public TestSession saveSession(TestSession session) {
        return sessionRepository.save(session);
    }

    @Transactional
    public TestSession completeSession(Long sessionId) {
        TestSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session != null) {
            if (session.getStatus() == Status.FINISHED || session.getStatus() == Status.TIMEOUT) {
                return session;
            }

            if (isSessionExpired(session)) {
                return handleTimeout(session);
            }

            session.setCompletedAt(LocalDateTime.now());
            session.setStatus(Status.FINISHED);

            calculateResults(session);
            
            TestSession savedSession = sessionRepository.save(session);
            
            // Auto-generate certificate if enabled
            if (savedSession.getQuizSnapshot().getQuiz().isCertificateEnabled()) {
                // Use username as default display name, user can update it later if we add that feature
                certificateService.generateCertificate(savedSession.getSessionId(), savedSession.getUser().getUsername());
            }

            return savedSession;
        }
        return null;
    }

    @Transactional
    public TestSession handleTimeout(TestSession session) {
        session.setStatus(Status.TIMEOUT);
        session.setCompletedAt(session.getExpiresAt() != null ? session.getExpiresAt() : LocalDateTime.now());
        calculateResults(session);
        
        TestSession savedSession = sessionRepository.save(session);
        
        // Auto-generate certificate if enabled (even on timeout if they passed?)
        // For now, let's assume yes if they have a score
        if (savedSession.getQuizSnapshot().getQuiz().isCertificateEnabled()) {
             certificateService.generateCertificate(savedSession.getSessionId(), savedSession.getUser().getUsername());
        }
        
        return savedSession;
    }

    private void calculateResults(TestSession session) {
        List<UserAnswer> answers = userAnswerRepository.findBySessionSessionId(session.getSessionId());
        long correctCount = answers.stream().filter(UserAnswer::getIsCorrect).count();

        session.setTotalQuestions(answers.size());
        session.setCorrectAnswers((int) correctCount);
        session.setIqScore(calculateIQ(correctCount, answers.size()));

        Duration duration = Duration.between(session.getStartedAt(), session.getCompletedAt());
        session.setTimeTakenSeconds((int) duration.getSeconds());
    }

    public boolean isSessionExpired(TestSession session) {
        if (session.getExpiresAt() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(session.getExpiresAt());
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
