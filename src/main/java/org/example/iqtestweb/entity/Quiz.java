package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.iqtestweb.entity.enums.QuizStatus;
import org.example.iqtestweb.entity.enums.QuizTimeType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "questions")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "quiz_type_id")
    private QuizType quizType;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "attachment_id", referencedColumnName = "id")
    private Attachment attachment;

    @Enumerated(EnumType.STRING)
    private QuizStatus status = QuizStatus.STOPPED;

    @Enumerated(EnumType.STRING)
    private QuizTimeType timeType = QuizTimeType.NO_LIMIT;

    private Integer timeLimitSeconds; // Used when timeType is TOTAL_TIME

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "certificate_enabled")
    private boolean certificateEnabled = false;

    @Column(name = "certificate_title")
    private String certificateTitle;

    @Column(name = "passing_score")
    private Integer passingScore;

    @Column(name = "certificate_template_name")
    private String certificateTemplateName; // For future extensibility

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
