package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.*;
import org.example.iqtestweb.repository.QuizSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizSnapshotService {

    private final QuizSnapshotRepository quizSnapshotRepository;
    private final QuestionSnapshotService questionSnapshotService;
    private final AnswerOptionSnapshotService answerOptionSnapshotService;
    private final QuestionService questionService;
    private final AnswerOptionService answerOptionService;

    @Transactional
    public QuizSnapshot getOrCreateSnapshot(Quiz quiz) {
        // Check if a snapshot exists for this quiz
        Optional<QuizSnapshot> existingSnapshot = quizSnapshotRepository.findTopByQuizOrderByCreatedAtDesc(quiz);

        if (existingSnapshot.isPresent()) {
            QuizSnapshot snapshot = existingSnapshot.get();
            // If the quiz hasn't been updated since the snapshot was created, reuse it
            if (quiz.getUpdatedAt() != null && snapshot.getOriginalQuizUpdatedAt() != null &&
                    !quiz.getUpdatedAt().isAfter(snapshot.getOriginalQuizUpdatedAt())) {
                return snapshot;
            }
        }

        // Create a new snapshot
        return createSnapshot(quiz);
    }

    private QuizSnapshot createSnapshot(Quiz quiz) {
        QuizSnapshot snapshot = new QuizSnapshot();
        snapshot.setQuiz(quiz);
        snapshot.setTitle(quiz.getName());
        snapshot.setQuizType(quiz.getQuizType());
        snapshot.setAttachment(quiz.getAttachment());
        snapshot.setTimeType(quiz.getTimeType());
        snapshot.setTimeLimitSeconds(quiz.getTimeLimitSeconds());
        
        // Snapshot psychometric settings
        snapshot.setAgeFactorEnabled(quiz.isAgeFactorEnabled());
        snapshot.setCustomFormulaEnabled(quiz.isCustomFormulaEnabled());
        snapshot.setKCoeff(quiz.getKCoeff());
        snapshot.setBCoeff(quiz.getBCoeff());

        snapshot.setOriginalQuizUpdatedAt(quiz.getUpdatedAt() != null ? quiz.getUpdatedAt() : LocalDateTime.now());
        snapshot.setCreatedAt(LocalDateTime.now());

        snapshot = quizSnapshotRepository.save(snapshot);

        // Create snapshots for questions and answer options
        List<Question> questions = questionService.getQuestionsByQuizId(quiz.getId());
        for (Question question : questions) {
            QuestionSnapshot qSnapshot = new QuestionSnapshot();
            qSnapshot.setQuizSnapshot(snapshot);
            qSnapshot.setQuestionText(question.getQuestionText());
            qSnapshot.setAttachment(question.getAttachment());
            qSnapshot.setDifficultyLevel(question.getDifficultyLevel());
            qSnapshot.setPoints(question.getPoints());
            qSnapshot.setOriginalQuestionId(question.getQuestionId());
            qSnapshot.setQuestionType(question.getQuestionType());
            qSnapshot.setTimeLimitSeconds(question.getTimeLimitSeconds());
            qSnapshot = questionSnapshotService.save(qSnapshot);

            List<AnswerOption> options = answerOptionService.findActiveByQuestionId(question.getQuestionId());
            for (AnswerOption option : options) {
                AnswerOptionSnapshot optSnapshot = new AnswerOptionSnapshot();
                optSnapshot.setQuestionSnapshot(qSnapshot);
                optSnapshot.setOptionText(option.getOptionText());
                optSnapshot.setAttachment(option.getAttachment());
                optSnapshot.setIsCorrect(option.getIsCorrect());
                optSnapshot.setOptionOrder(option.getOptionOrder());
                optSnapshot.setOriginalOptionId(option.getOptionId());
                answerOptionSnapshotService.save(optSnapshot);
            }
        }

        return snapshot;
    }
}
