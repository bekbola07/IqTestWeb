package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.iqtestweb.entity.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_sessions")
@Data
@NoArgsConstructor
@ToString(exclude = {"userAnswers"})
public class TestSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_snapshot_id")
    private QuizSnapshot quizSnapshot;

    @Column(name = "started_at")
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer iqScore;
    private Integer timeTakenSeconds;

    @Column(name = "calculation_log", columnDefinition = "TEXT")
    private String calculationLog;

    @Enumerated(EnumType.STRING)
    private Status status = Status.IN_PROGRESS;

    @PreRemove
    public void preRemove() {
        this.status = Status.DELETED;
    }
}
