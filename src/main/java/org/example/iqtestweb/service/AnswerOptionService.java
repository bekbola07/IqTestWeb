package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.AnswerOption;
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

    @Transactional
    public void deleteByQuestionId(Long questionId) {
        answerOptionRepository.deleteByQuestionQuestionId(questionId);
    }

    public void saveAll(List<AnswerOption> answerOptions) {
        answerOptionRepository.saveAll(answerOptions);
    }

    public AnswerOption getAnswerOptionById(Long id) {
        return answerOptionRepository.findById(id).orElse(null);
    }

    public void softDeleteByQuestionId(Long questionId) {
        answerOptionRepository.softDeleteByQuestionId(questionId);
    }
}
