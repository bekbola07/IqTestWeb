package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.QuestionSnapshot;
import org.example.iqtestweb.repository.QuestionSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionSnapshotService {

    private final QuestionSnapshotRepository questionSnapshotRepository;

    /**
     * Snapshot saqlash
     */
    @Transactional
    public QuestionSnapshot save(QuestionSnapshot snapshot) {
        return questionSnapshotRepository.save(snapshot);
    }

    /**
     * Bir nechta snapshot'larni saqlash
     */
    @Transactional
    public List<QuestionSnapshot> saveAll(List<QuestionSnapshot> snapshots) {
        return questionSnapshotRepository.saveAll(snapshots);
    }

    /**
     * ID bo'yicha snapshot topish
     */
    public Optional<QuestionSnapshot> findById(Long snapshotId) {
        return questionSnapshotRepository.findById(snapshotId);
    }

    /**
     * ID bo'yicha snapshot olish (exception bilan)
     */
    public QuestionSnapshot getById(Long snapshotId) {
        return questionSnapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new RuntimeException("Question snapshot not found with id: " + snapshotId));
    }

    /**
     * Quiz snapshot bo'yicha barcha question snapshot'larni topish
     */
    public List<QuestionSnapshot> findByQuizSnapshotId(Long quizSnapshotId) {
        return questionSnapshotRepository.findByQuizSnapshot_Id(quizSnapshotId);
    }

    /**
     * Original question ID bo'yicha snapshot'larni topish
     * (Tarixiy ma'lumotlarni ko'rish uchun)
     */
    public List<QuestionSnapshot> findByOriginalQuestionId(Long originalQuestionId) {
        return questionSnapshotRepository.findByOriginalQuestionId(originalQuestionId);
    }

    /**
     * Snapshot o'chirish (odatda kerak bo'lmaydi, lekin admin funksiyasi uchun)
     */
    @Transactional
    public void deleteById(Long snapshotId) {
        questionSnapshotRepository.deleteById(snapshotId);
    }

    /**
     * Snapshot mavjudligini tekshirish
     */
    public boolean existsById(Long snapshotId) {
        return questionSnapshotRepository.existsById(snapshotId);
    }
}
