package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.iqtestweb.entity.enums.DifficultyLevel;

import java.util.List;

@Entity
@Table(name = "question_categories")
@Data
@NoArgsConstructor
public class QuestionCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    private String categoryName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @OneToMany(mappedBy = "questionCategory", cascade = CascadeType.ALL)
    private List<Question> questions;
}