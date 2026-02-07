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
@ToString(exclude = {"questions", "authorCertificate"})
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

    @OneToOne(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private AuthorCertificate authorCertificate;

    @Transient
    private String certificateTitle;

    @Transient
    private Integer passingScore;

    @Transient
    private String certificateDescription;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PostLoad
    public void postLoad() {
        if (authorCertificate != null) {
            this.certificateTitle = authorCertificate.getTitle();
            this.passingScore = authorCertificate.getPassingScore();
            this.certificateDescription = authorCertificate.getDescription();
        }
    }
}
