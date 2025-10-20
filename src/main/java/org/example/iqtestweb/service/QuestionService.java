package org.example.iqtestweb.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.AnswerOption;
import org.example.iqtestweb.entity.Question;
import org.example.iqtestweb.repository.AnswerOptionRepository;
import org.example.iqtestweb.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    private final AnswerOptionRepository answerOptionRepository;

    public List<Question> getAllActiveQuestions() {
        return questionRepository.findByIsActiveTrue();
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
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

    @Transactional
    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }
}