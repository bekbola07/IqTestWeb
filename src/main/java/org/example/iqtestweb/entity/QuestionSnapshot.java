package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.example.iqtestweb.entity.enums.DifficultyLevel;
import org.example.iqtestweb.entity.enums.QuestionType;
import org.example.iqtestweb.entity.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "question_snapshots")
@Data
@ToString(exclude = "answerOptionSnapshots")
public class QuestionSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long snapshotId;

    @ManyToOne
    @JoinColumn(name = "quiz_snapshot_id")
    private QuizSnapshot quizSnapshot;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @ManyToOne
    @JoinColumn(name = "attachment_id")
    private Attachment attachment;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    private Integer timeLimitSeconds = 60;

    private Integer points;

    // Original question ID (reference only)
    private Long originalQuestionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "questionSnapshot", cascade = CascadeType.ALL)
    private List<AnswerOptionSnapshot> answerOptionSnapshots;

    @PreRemove
    public void preRemove() {
        this.status = Status.DELETED;
    }
}
