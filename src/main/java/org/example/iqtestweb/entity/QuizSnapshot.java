package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.iqtestweb.entity.enums.QuizTimeType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quiz_snapshots")
@Data
@NoArgsConstructor
@ToString(exclude = "questionSnapshots")
public class QuizSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "original_quiz_id")
    private Quiz quiz;

    @Column(name = "title")
    private String title;

    @ManyToOne
    @JoinColumn(name = "quiz_type_id")
    private QuizType quizType;

    @ManyToOne
    @JoinColumn(name = "attachment_id")
    private Attachment attachment;

    @Enumerated(EnumType.STRING)
    private QuizTimeType timeType;

    private Integer timeLimitSeconds;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "original_quiz_updated_at")
    private LocalDateTime originalQuizUpdatedAt;

    @OneToMany(mappedBy = "quizSnapshot", cascade = CascadeType.ALL)
    private List<QuestionSnapshot> questionSnapshots;
}
