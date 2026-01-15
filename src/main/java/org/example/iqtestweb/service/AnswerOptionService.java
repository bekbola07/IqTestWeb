package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.AnswerOption;
import org.example.iqtestweb.entity.Question;
import org.example.iqtestweb.entity.enums.Status;
import org.example.iqtestweb.repository.AnswerOptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnswerOptionService {

    private final AnswerOptionRepository answerOptionRepository;

    public List<AnswerOption> findByQuestionId(Long questionId) {
        return answerOptionRepository.findByQuestionQuestionId(questionId);
    }

    public List<AnswerOption> findActiveByQuestionId(Long questionId) {
        return answerOptionRepository.findByQuestionQuestionIdAndStatus(questionId, Status.ACTIVE);
    }

    @Transactional
    public void deleteByQuestionId(Long questionId) {
        // Update quiz timestamp before deleting options
        List<AnswerOption> options = answerOptionRepository.findByQuestionQuestionId(questionId);
        if (!options.isEmpty()) {
            Question question = options.get(0).getQuestion();
            if (question != null && question.getQuiz() != null) {
                question.getQuiz().preUpdate();
            }
        }
        answerOptionRepository.deleteByQuestionQuestionId(questionId);
    }

    @Transactional
    public void softDeleteByQuestionId(Long questionId) {
        // Update quiz timestamp
        List<AnswerOption> options = answerOptionRepository.findByQuestionQuestionId(questionId);
        if (!options.isEmpty()) {
            Question question = options.get(0).getQuestion();
            if (question != null && question.getQuiz() != null) {
                question.getQuiz().preUpdate();
            }
        }
        answerOptionRepository.softDeleteByQuestionId(questionId);
    }

    public void saveAll(List<AnswerOption> answerOptions) {
        if (!answerOptions.isEmpty()) {
            Question question = answerOptions.get(0).getQuestion();
            if (question != null && question.getQuiz() != null) {
                question.getQuiz().preUpdate();
            }
        }
        answerOptionRepository.saveAll(answerOptions);
    }

    public AnswerOption getAnswerOptionById(Long id) {
        return answerOptionRepository.findById(id).orElse(null);
    }
}
