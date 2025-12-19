package org.example.iqtestweb.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.AnswerOption;
import org.example.iqtestweb.entity.Question;
import org.example.iqtestweb.entity.QuestionCategory;
import org.example.iqtestweb.entity.enums.Status;
import org.example.iqtestweb.repository.AnswerOptionRepository;
import org.example.iqtestweb.repository.QuestionCategoryRepository;
import org.example.iqtestweb.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerOptionService answerOptionService;
    private final QuestionCategoryRepository categoryRepository;

    public List<Question> getAllActiveQuestions() {
        return questionRepository.findAll().stream()
                .filter(question -> question.getStatus() == Status.ACTIVE)
                .collect(Collectors.toList());
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id).orElse(null);
    }

    @Transactional
    public Question saveQuestion(Question question) {
        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        answerOptionService.deleteByQuestionId(id);
        questionRepository.deleteById(id);
    }

    public List<QuestionCategory> getAllCategories() {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getStatus() == Status.ACTIVE)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuestionCategory saveCategory(QuestionCategory category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.findById(id).ifPresent(category -> {
            category.setStatus(Status.DELETED);
            categoryRepository.save(category);
        });
    }

    public List<Question> getQuestionsByCategoryId(Long categoryId) {
        return questionRepository.getQuestionsByQuestionCategory_CategoryId(categoryId);
    }

    public List<Question> getQuestionsByQuizId(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }
}
