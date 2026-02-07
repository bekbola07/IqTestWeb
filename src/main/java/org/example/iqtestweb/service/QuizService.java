package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.Attachment;
import org.example.iqtestweb.entity.AuthorCertificate;
import org.example.iqtestweb.entity.Quiz;
import org.example.iqtestweb.entity.enums.QuizStatus;
import org.example.iqtestweb.entity.enums.QuizTimeType;
import org.example.iqtestweb.repository.AuthorCertificateRepository;
import org.example.iqtestweb.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final AuthorCertificateRepository authorCertificateRepository;

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .filter(quiz -> quiz.getStatus() != QuizStatus.DELETED)
                .collect(Collectors.toList());
    }

    public List<Quiz> getQuizzesByUserId(Long userId) {
        return quizRepository.findByUserUserId(userId).stream()
                .filter(quiz -> quiz.getStatus() != QuizStatus.DELETED)
                .collect(Collectors.toList());
    }

    @Transactional
    public Quiz saveQuiz(Quiz quiz) {
        quiz.preUpdate();
        Quiz savedQuiz = quizRepository.save(quiz);
        
        handleCertificateUpdate(savedQuiz);
        
        return savedQuiz;
    }

    @Transactional
    public Quiz updateQuizSettings(Long id, Quiz quizUpdates, Attachment newAttachment, boolean removeImage) {
        Quiz quiz = getQuizById(id);
        if (quiz == null) throw new IllegalArgumentException("Quiz not found");

        // Business Rule: Cannot change duration/structure if STARTED
        if (quiz.getStatus() == QuizStatus.STARTED) {
            if (quiz.getTimeType() != quizUpdates.getTimeType() ||
                !Objects.equals(quiz.getTimeLimitSeconds(), quizUpdates.getTimeLimitSeconds())) {
                throw new IllegalStateException("Cannot change time settings of a published (STARTED) quiz. Please stop the quiz first.");
            }
            // We could also block QuizType changes here
            if (!Objects.equals(quiz.getQuizType().getId(), quizUpdates.getQuizType().getId())) {
                throw new IllegalStateException("Cannot change quiz type of a published quiz.");
            }
        }

        quiz.setName(quizUpdates.getName());
        
        // Only update structural fields if not STARTED
        if (quiz.getStatus() != QuizStatus.STARTED) {
            quiz.setQuizType(quizUpdates.getQuizType());
            quiz.setTimeType(quizUpdates.getTimeType());
            quiz.setTimeLimitSeconds(quizUpdates.getTimeLimitSeconds());
        }

        quiz.setCertificateEnabled(quizUpdates.isCertificateEnabled());
        
        // Update transient fields from updates to use in handleCertificateUpdate
        quiz.setCertificateTitle(quizUpdates.getCertificateTitle());
        quiz.setPassingScore(quizUpdates.getPassingScore());
        quiz.setCertificateDescription(quizUpdates.getCertificateDescription());

        handleCertificateUpdate(quiz);

        if (removeImage) {
            quiz.setAttachment(null);
        } else if (newAttachment != null) {
            quiz.setAttachment(newAttachment);
        }

        return quizRepository.save(quiz);
    }

    private void handleCertificateUpdate(Quiz quiz) {
        if (quiz.isCertificateEnabled()) {
            AuthorCertificate authorCertificate = quiz.getAuthorCertificate();
            if (authorCertificate == null) {
                // Try to find by quiz ID if not loaded in relationship
                authorCertificate = authorCertificateRepository.findByQuizId(quiz.getId()).orElse(null);
            }
            
            if (authorCertificate == null) {
                authorCertificate = AuthorCertificate.builder()
                        .quiz(quiz)
                        .createdBy(quiz.getUser())
                        .templatePath("certificate/template") // Default template
                        .build();
            }
            
            // Update fields
            if (quiz.getCertificateTitle() != null && !quiz.getCertificateTitle().isEmpty()) {
                authorCertificate.setTitle(quiz.getCertificateTitle());
            } else if (authorCertificate.getTitle() == null) {
                 authorCertificate.setTitle(quiz.getName() + " Certificate");
            }
            
            authorCertificate.setPassingScore(quiz.getPassingScore());
            authorCertificate.setDescription(quiz.getCertificateDescription());
            
            authorCertificateRepository.save(authorCertificate);
            quiz.setAuthorCertificate(authorCertificate); // Update reference
        }
    }

    @Transactional
    public void startQuiz(Long id) {
        Quiz quiz = getQuizById(id);
        if (quiz == null) throw new IllegalArgumentException("Quiz not found");

        // Business Rule: Validate Duration before Publish
        if (quiz.getTimeType() == QuizTimeType.TOTAL_TIME) {
            if (quiz.getTimeLimitSeconds() == null || quiz.getTimeLimitSeconds() <= 0) {
                throw new IllegalStateException("Duration (seconds) is required for 'Total Time' quizzes.");
            }
        }
        
        // Additional validation: Ensure quiz has questions
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
             throw new IllegalStateException("Cannot start a quiz with no questions.");
        }

        quiz.setStatus(QuizStatus.STARTED);
        quizRepository.save(quiz);
    }

    @Transactional
    public void stopQuiz(Long id) {
        Quiz quiz = getQuizById(id);
        if (quiz != null) {
            quiz.setStatus(QuizStatus.STOPPED);
            quizRepository.save(quiz);
        }
    }

    public Quiz getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id).orElse(null);
        if (quiz != null && quiz.getStatus() == QuizStatus.DELETED) {
            return null;
        }
        return quiz;
    }

    @Transactional
    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id).orElse(null);
        if (quiz != null) {
            quiz.setStatus(QuizStatus.DELETED);
            quizRepository.save(quiz);
        }
    }
}
