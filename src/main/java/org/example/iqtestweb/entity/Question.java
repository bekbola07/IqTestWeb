package org.example.iqtestweb.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.iqtestweb.entity.enums.DifficultyLevel;
import org.example.iqtestweb.entity.enums.QuestionType;
import org.example.iqtestweb.entity.enums.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category", nullable = false)
    private QuestionCategory questionCategory;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "attachment_id", referencedColumnName = "id")
    private Attachment attachment;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    private Integer timeLimitSeconds = 60;
    private Integer points = 1;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<AnswerOption> answerOptions = new ArrayList<>();

    @PreRemove
    public void preRemove() {
        this.status = Status.DELETED;
    }
}
