package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.iqtestweb.entity.enums.DifficultyLevel;
import org.example.iqtestweb.entity.enums.QuestionType;
import org.example.iqtestweb.entity.enums.Status;

import java.time.LocalDateTime;

// Yangi entity yarating
@Entity
@Table(name = "question_snapshots")
@Data
public class QuestionSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long snapshotId;

    @ManyToOne
    @JoinColumn(name = "test_session_id")
    private TestSession testSession;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    private String questionImageUrl;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType = QuestionType.TEXT;

    private Integer timeLimitSeconds = 60;

    private Integer points;

    // Original question ID (reference only)
    private Long originalQuestionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @PreRemove
    public void preRemove() {
        this.status = Status.DELETED;
    }
}

