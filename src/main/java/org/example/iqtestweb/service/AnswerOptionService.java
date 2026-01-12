package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.AnswerOption;
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
        answerOptionRepository.deleteByQuestionQuestionId(questionId);
    }

    @Transactional
    public void softDeleteByQuestionId(Long questionId) {
        answerOptionRepository.softDeleteByQuestionId(questionId);
    }

    public void saveAll(List<AnswerOption> answerOptions) {
        answerOptionRepository.saveAll(answerOptions);
    }

    public AnswerOption getAnswerOptionById(Long id) {
        return answerOptionRepository.findById(id).orElse(null);
    }
}
