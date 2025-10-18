package org.example.iqtestweb.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.iqtestweb.entity.enums.DifficultyLevel;
import org.example.iqtestweb.entity.enums.QuestionType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private QuestionCategory category;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    private String questionImageUrl;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType = QuestionType.TEXT;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    private Integer timeLimitSeconds = 60;
    private Integer points = 1;
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<AnswerOption> answerOptions;
}
