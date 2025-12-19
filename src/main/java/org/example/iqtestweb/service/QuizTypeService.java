package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.QuizType;
import org.example.iqtestweb.entity.enums.Status;
import org.example.iqtestweb.repository.QuizTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizTypeService {

    private final QuizTypeRepository quizTypeRepository;

    public List<QuizType> getAllQuizTypes() {
        return quizTypeRepository.findAll().stream()
                .filter(quizType -> quizType.getStatus() == Status.ACTIVE)
                .collect(Collectors.toList());
    }

    public QuizType saveQuizType(QuizType quizType) {
        return quizTypeRepository.save(quizType);
    }

    public QuizType getQuizTypeById(Long id) {
        return quizTypeRepository.findById(id).orElse(null);
    }

    public void deleteQuizType(Long id) {
        quizTypeRepository.findById(id).ifPresent(quizType -> {
            quizType.setStatus(Status.DELETED);
            quizTypeRepository.save(quizType);
        });
    }
}
