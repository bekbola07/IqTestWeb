package org.example.iqtestweb.service;

import jakarta.transaction.Transactional;
import org.example.iqtestweb.entity.AnswerOption;
import org.example.iqtestweb.entity.Question;
import org.example.iqtestweb.repository.AnswerOptionRepository;
import org.example.iqtestweb.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerOptionRepository answerOptionRepository;

    public List<Question> getAllActiveQuestions() {
        return questionRepository.findByIsActiveTrue();
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id).orElse(null);
    }

    public List<AnswerOption> getOptionsForQuestion(Long questionId) {
        return answerOptionRepository.findByQuestionQuestionId(questionId);
    }

    @Transactional
    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }
}