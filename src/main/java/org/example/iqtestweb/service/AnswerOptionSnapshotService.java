package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.AnswerOptionSnapshot;
import org.example.iqtestweb.repository.AnswerOptionSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnswerOptionSnapshotService {

    private final AnswerOptionSnapshotRepository answerOptionSnapshotRepository;

    /**
     * Snapshot saqlash
     */
    @Transactional
    public AnswerOptionSnapshot save(AnswerOptionSnapshot snapshot) {
        return answerOptionSnapshotRepository.save(snapshot);
    }

    /**
     * Bir nechta snapshot'larni saqlash
     */
    @Transactional
    public List<AnswerOptionSnapshot> saveAll(List<AnswerOptionSnapshot> snapshots) {
        return answerOptionSnapshotRepository.saveAll(snapshots);
    }

    /**
     * ID bo'yicha snapshot topish
     */
    public Optional<AnswerOptionSnapshot> findById(Long snapshotId) {
        return answerOptionSnapshotRepository.findById(snapshotId);
    }

    /**
     * ID bo'yicha snapshot olish (exception bilan)
     */
    public AnswerOptionSnapshot getById(Long snapshotId) {
        return answerOptionSnapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new RuntimeException("Answer option snapshot not found with id: " + snapshotId));
    }

    /**
     * Question snapshot bo'yicha barcha answer option snapshot'larni topish
     */
    public List<AnswerOptionSnapshot> findByQuestionSnapshotId(Long questionSnapshotId) {
        return answerOptionSnapshotRepository.findByQuestionSnapshot_SnapshotId(questionSnapshotId);
    }

    /**
     * Question snapshot bo'yicha snapshot'lar sonini hisoblash
     */
    public long countByQuestionSnapshotId(Long questionSnapshotId) {
        return answerOptionSnapshotRepository.countByQuestionSnapshot_SnapshotId(questionSnapshotId);
    }

    /**
     * Question snapshot bo'yicha to'g'ri javobni topish
     */
    public Optional<AnswerOptionSnapshot> findCorrectAnswerByQuestionSnapshotId(Long questionSnapshotId) {
        return answerOptionSnapshotRepository.findByQuestionSnapshot_SnapshotIdAndIsCorrect(questionSnapshotId, true);
    }

    /**
     * Original answer option ID bo'yicha snapshot'larni topish
     * (Tarixiy ma'lumotlarni ko'rish uchun)
     */
    public List<AnswerOptionSnapshot> findByOriginalOptionId(Long originalOptionId) {
        return answerOptionSnapshotRepository.findByOriginalOptionId(originalOptionId);
    }

    /**
     * Snapshot o'chirish (odatda kerak bo'lmaydi)
     */
    @Transactional
    public void deleteById(Long snapshotId) {
        answerOptionSnapshotRepository.deleteById(snapshotId);
    }

    /**
     * Question snapshot bo'yicha barcha answer option snapshot'larni o'chirish
     */
    @Transactional
    public void deleteByQuestionSnapshotId(Long questionSnapshotId) {
        answerOptionSnapshotRepository.deleteByQuestionSnapshot_SnapshotId(questionSnapshotId);
    }

    /**
     * Snapshot mavjudligini tekshirish
     */
    public boolean existsById(Long snapshotId) {
        return answerOptionSnapshotRepository.existsById(snapshotId);
    }

    /**
     * Option order bo'yicha topish (saralangan holda)
     */
    public List<AnswerOptionSnapshot> findByQuestionSnapshotIdOrderByOptionOrder(Long questionSnapshotId) {
        return answerOptionSnapshotRepository.findByQuestionSnapshot_SnapshotIdOrderByOptionOrderAsc(questionSnapshotId);
    }

    public List<AnswerOptionSnapshot> findByOriginalOptionIds(List<Long> originalOptionIds) {
        return answerOptionSnapshotRepository.findByOriginalOptionIdIn(originalOptionIds);
    }
}
