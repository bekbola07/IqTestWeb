package org.example.iqtestweb.entity.dto;

import lombok.Data;
import org.example.iqtestweb.entity.Question;
import org.example.iqtestweb.entity.QuestionCategory;
import org.example.iqtestweb.entity.enums.DifficultyLevel;

import java.util.List;

@Data
public class CategoryDTO {
    private Long categoryId;

    private String categoryName;

    private String description;

    private DifficultyLevel difficultyLevel;
    
    private List<Question> questions;
    
    public CategoryDTO(QuestionCategory category, List<Question> questions) {
        this.categoryId = category.getCategoryId();
        this.categoryName = category.getCategoryName();
        this.description = category.getDescription();
        this.difficultyLevel = category.getDifficultyLevel();
        this.questions = questions;
    }
}