package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_answers")
@Data
@NoArgsConstructor
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private TestSession session;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne
    @JoinColumn(name = "selected_option_id")
    private AnswerOption selectedOption;

    private Boolean isCorrect;
    private Integer timeTakenSeconds;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt = LocalDateTime.now();
}