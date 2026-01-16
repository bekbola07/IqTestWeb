package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.iqtestweb.entity.enums.Status;

@Entity
@Table(name = "answer_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long optionId;

    @ManyToOne()
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String optionText;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "attachment_id", referencedColumnName = "id")
    private Attachment attachment;

    private Boolean isCorrect = false;
    private Integer optionOrder;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    public AnswerOption(Question question, String optionText, Boolean isCorrect, Integer optionOrder) {
        this.question = question;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
        this.optionOrder = optionOrder;
    }
     public AnswerOption(Question question, String optionText, Attachment attachment, Boolean isCorrect, Integer optionOrder) {
        this.question = question;
        this.optionText = optionText;
        this.attachment = attachment;
        this.isCorrect = isCorrect;
        this.optionOrder = optionOrder;
    }

    @PreRemove
    public void preRemove() {
        this.status = Status.DELETED;
    }
}
