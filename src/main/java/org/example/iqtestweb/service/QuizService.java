package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.Quiz;
import org.example.iqtestweb.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    public List<Quiz> getQuizzesByUserId(Long userId) {
        return quizRepository.findByUserUserId(userId);
    }

    public Quiz saveQuiz(Quiz quiz) {
        quiz.preUpdate();
        return quizRepository.save(quiz);
    }

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id).orElse(null);
    }

    public void deleteQuiz(Long id) {
        quizRepository.deleteById(id);
    }
}
