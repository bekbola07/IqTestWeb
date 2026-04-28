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

    // New DTO for completion result
    public static class TestSessionCompletionResult {
        private final TestSession session;
        private final boolean profileDataRequired;

        public TestSessionCompletionResult(TestSession session, boolean profileDataRequired) {
            this.session = session;
            this.profileDataRequired = profileDataRequired;
        }

        public TestSession getSession() {
            return session;
        }

        public boolean isProfileDataRequired() {
            return profileDataRequired;
        }
    }

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
    public TestSessionCompletionResult completeSession(Long sessionId) {
        TestSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return new TestSessionCompletionResult(null, false); // Session not found
        }

        if (session.getStatus() == Status.FINISHED || session.getStatus() == Status.TIMEOUT) {
            return new TestSessionCompletionResult(session, false); // Already completed
        }

        if (isSessionExpired(session)) {
            TestSession timedOutSession = handleTimeout(session);
            return new TestSessionCompletionResult(timedOutSession, false); // Timed out, results calculated
        }

        // Set completedAt IMMEDIATELY when the test is finished, before any data checks.
        // This ensures timeTakenSeconds doesn't include form filling time.
        if (session.getCompletedAt() == null) {
            session.setCompletedAt(LocalDateTime.now());
            sessionRepository.save(session); // Save it early
        }

        // Check if age is required before calculating results
        if (session.getQuizSnapshot().isAgeFactorEnabled() && session.getUser().getAge() == null) {
            return new TestSessionCompletionResult(session, true); // Profile data (age) is required
        }

        // If age is available or not required, proceed with calculation
        session.setStatus(Status.FINISHED);

        calculateResults(session); // This will now use the age if available and the early completedAt

        TestSession savedSession = sessionRepository.save(session);

        // Auto-generate certificate if enabled
        if (savedSession.getQuizSnapshot().getQuiz().isCertificateEnabled()) {
            certificateService.generateCertificate(savedSession.getSessionId(), savedSession.getUser().getUsername());
        }

        return new TestSessionCompletionResult(savedSession, false);
    }

    @Transactional
    public TestSessionCompletionResult completeSessionWithAge(Long sessionId, Integer age) {
        TestSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null) {
            return new TestSessionCompletionResult(null, false);
        }

        // Update user's age
        User user = session.getUser();
        if (user != null && age != null && age > 0) { // Basic validation
            user.setAge(age);
        }

        // Now attempt to complete the session again
        return completeSession(sessionId);
    }


    @Transactional
    public TestSession handleTimeout(TestSession session) {
        session.setStatus(Status.TIMEOUT);
        // For timeout, we use expiresAt or current time
        if (session.getCompletedAt() == null) {
            session.setCompletedAt(session.getExpiresAt() != null ? session.getExpiresAt() : LocalDateTime.now());
        }
        
        // For timed-out sessions, we proceed with calculation even if age is missing
        calculateResults(session);
        
        TestSession savedSession = sessionRepository.save(session);
        
        if (savedSession.getQuizSnapshot().getQuiz().isCertificateEnabled()) {
             certificateService.generateCertificate(savedSession.getSessionId(), savedSession.getUser().getUsername());
        }
        
        return savedSession;
    }

    private void calculateResults(TestSession session) {
        List<UserAnswer> answers = userAnswerRepository.findBySessionSessionId(session.getSessionId());
        QuizSnapshot snapshot = session.getQuizSnapshot();
        
        // Sum points for correct answers
        int userScore = answers.stream()
                .filter(UserAnswer::getIsCorrect)
                .mapToInt(ua -> ua.getQuestionSnapshot().getPoints() != null ? ua.getQuestionSnapshot().getPoints() : 1)
                .sum();
        
        // Calculate max possible score from snapshot
        int maxScore = snapshot.getQuestionSnapshots().stream()
                .mapToInt(qs -> qs.getPoints() != null ? qs.getPoints() : 1)
                .sum();

        session.setTotalQuestions(snapshot.getQuestionSnapshots().size());
        session.setCorrectAnswers((int) answers.stream().filter(UserAnswer::getIsCorrect).count());
        
        // Use psychometric calculation engine
        IqCalculationResult calcResult = calculateFinalIQ(
            (double) userScore,
            (double) maxScore,
            session.getUser().getAge(), // Pass user's age
            snapshot.isAgeFactorEnabled(),
            snapshot.isCustomFormulaEnabled(),
            snapshot.getKCoeff(),
            snapshot.getBCoeff()
        );

        session.setIqScore(calcResult.finalIQ);
        session.setCalculationLog(calcResult.calculationLog);

        // Time taken is now calculated based on the early completedAt set in completeSession()
        Duration duration = Duration.between(session.getStartedAt(), session.getCompletedAt());
        session.setTimeTakenSeconds((int) duration.getSeconds());
    }

    private IqCalculationResult calculateFinalIQ(double userScore, double maxScore, Integer age, 
                                                boolean ageFactorEnabled, boolean customFormulaEnabled, 
                                                Double kCoeff, Double bCoeff) {
        
        if (maxScore <= 0) {
            return new IqCalculationResult(40, "Error: Max score is zero.");
        }

        StringBuilder log = new StringBuilder();

        // 1. Age Coefficient (C_age)
        double cAge = 1.0;
        if (ageFactorEnabled) {
            if (age == null) {
                log.append("Age factor enabled but user age missing (defaulting to 1.0). ");
            } else {
                if (age < 18) {
                    cAge = 1.0 + (18.0 - age) / 20.0;
                } else if (age > 30) {
                    cAge = 1.0 + (age - 30.0) / 100.0;
                }
                log.append("Age Factor Applied (C_age=").append(String.format("%.2f", cAge)).append("). ");
            }
        } else {
            log.append("Age Factor Disabled. ");
        }

        // 2. Coefficients
        double k = (customFormulaEnabled && kCoeff != null) ? kCoeff : 60.0;
        double b = (customFormulaEnabled && bCoeff != null) ? bCoeff : 70.0;
        
        if (customFormulaEnabled) {
            log.append("Custom Formula Applied (K=").append(k).append(", B=").append(b).append("). ");
        } else {
            log.append("Standard Formula Applied (K=60, B=70). ");
        }

        // 3. Core Formula: Result = (userScore / maxScore) * k * cAge + b
        double result = (userScore / maxScore) * k * cAge + b;

        // 4. Validation & Integrity
        int finalIQ = (int) Math.round(result);
        
        // Clamp between 40 and 160
        if (finalIQ < 40) finalIQ = 40;
        if (finalIQ > 160) finalIQ = 160;

        return new IqCalculationResult(finalIQ, log.toString().trim());
    }

    private static class IqCalculationResult {
        int finalIQ;
        String calculationLog;

        IqCalculationResult(int finalIQ, String calculationLog) {
            this.finalIQ = finalIQ;
            this.calculationLog = calculationLog;
        }
    }

    public boolean isSessionExpired(TestSession session) {
        if (session.getExpiresAt() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(session.getExpiresAt());
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
