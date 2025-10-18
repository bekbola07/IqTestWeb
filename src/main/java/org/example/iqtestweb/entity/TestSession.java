package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_sessions")
@Data
@NoArgsConstructor
public class TestSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "started_at")
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private Integer totalQuestions;
    private Integer correctAnswers;
    private Integer iqScore;
    private Integer timeTakenSeconds;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<UserAnswer> userAnswers;
}