package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.UserAnswer;
import org.example.iqtestweb.repository.UserAnswerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAnswerService {

    private final UserAnswerRepository userAnswerRepository;

    public UserAnswer saveUserAnswer(UserAnswer userAnswer) {
        return userAnswerRepository.save(userAnswer);
    }

    public List<UserAnswer> getUserAnswersBySessionId(Long sessionId) {
        return userAnswerRepository.findBySessionSessionId(sessionId);
    }
}
