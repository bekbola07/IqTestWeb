package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.Quiz;
import org.example.iqtestweb.entity.enums.QuizStatus;
import org.example.iqtestweb.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;

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

    public Quiz saveQuiz(Quiz quiz) {
        quiz.preUpdate();
        return quizRepository.save(quiz);
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
