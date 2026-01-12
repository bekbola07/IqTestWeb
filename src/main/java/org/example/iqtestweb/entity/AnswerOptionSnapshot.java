package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "answer_option_snapshots")
@Data
public class AnswerOptionSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long snapshotId;

    @ManyToOne
    @JoinColumn(name = "question_snapshot_id")
    private QuestionSnapshot questionSnapshot;

    @Column(columnDefinition = "TEXT")
    private String optionText;

    private String imageUrl;
    private Boolean isCorrect;
    private Integer optionOrder;

    // Original option ID (reference only)
    private Long originalOptionId;
}
