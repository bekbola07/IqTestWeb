package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answer_options")
@Data
@NoArgsConstructor
public class AnswerOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String optionText;

    private String optionImageUrl;
    private Boolean isCorrect = false;
    private Integer optionOrder;
}
