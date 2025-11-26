package org.example.iqtestweb.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.AnswerOption;
import org.example.iqtestweb.entity.Question;
import org.example.iqtestweb.entity.QuestionCategory;
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

    private final AnswerOptionRepository answerOptionRepository;

    private final QuestionCategoryRepository categoryRepository;

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

    public List<QuestionCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public QuestionCategory saveCategory(QuestionCategory category) {
        return categoryRepository.save(category);
    }

    public void saveAnswerOption(AnswerOption option) {
        answerOptionRepository.save(option);
    }

    public List<Question> getQuestionsByCategoryId(Long categoryId) {

        return questionRepository.getQuestionsByQuestionCategory_CategoryId(categoryId);
    }
    @Transactional
    public void updateQuestion(Long id, Question updatedQuestion) {
        Question existingQuestion = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with id: " + id));

        // Update the main question details
        existingQuestion.setQuestionText(updatedQuestion.getQuestionText());
        existingQuestion.setQuestionType(updatedQuestion.getQuestionType());
        existingQuestion.setPoints(updatedQuestion.getPoints());
        existingQuestion.setDifficultyLevel(updatedQuestion.getDifficultyLevel());
        existingQuestion.setQuestionCategory(updatedQuestion.getQuestionCategory());
        existingQuestion.setTimeLimitSeconds(updatedQuestion.getTimeLimitSeconds());
        existingQuestion.setQuestionImageUrl(updatedQuestion.getQuestionImageUrl());
        existingQuestion.setIsActive(updatedQuestion.getIsActive());

        // Create a map of updated options by their ID for efficient lookup
        Map<Long, AnswerOption> updatedOptionsMap = updatedQuestion.getAnswerOptions().stream()
                .collect(Collectors.toMap(AnswerOption::getOptionId, option -> option));

        // Iterate over the existing options and update them with the new values
        for (AnswerOption existingOption : existingQuestion.getAnswerOptions()) {
            AnswerOption updatedOption = updatedOptionsMap.get(existingOption.getOptionId());
            if (updatedOption != null) {
                existingOption.setOptionText(updatedOption.getOptionText());
                existingOption.setIsCorrect(updatedOption.getIsCorrect());
                existingOption.setImageUrl(updatedOption.getImageUrl());
            }
        }

        questionRepository.save(existingQuestion);
    }
}
