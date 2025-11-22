package org.example.iqtestweb.entity.dto;

import lombok.Data;
import org.example.iqtestweb.entity.AnswerOption;
import org.example.iqtestweb.entity.Question;
import org.example.iqtestweb.entity.QuestionCategory;
import org.example.iqtestweb.entity.enums.DifficultyLevel;
import org.example.iqtestweb.entity.enums.QuestionType;

import java.util.List;

@Data
public class QuestionDTO {
    private Long questionId;

    private String questionText;

    private DifficultyLevel difficultyLevel;

    private QuestionCategory questionCategory;

    private String questionImageUrl;

    private QuestionType questionType = QuestionType.TEXT;

    private Integer timeLimitSeconds = 60;
    private Integer points = 1;
    private Boolean isActive = true;
    private List<AnswerOption> answerOptions;

    public QuestionDTO(Question question, List<AnswerOption> answerOptions) {
        this.questionId = question.getQuestionId();
        this.questionText = question.getQuestionText();
        this.difficultyLevel = question.getDifficultyLevel();
        this.questionCategory = question.getQuestionCategory();
        this.questionImageUrl = question.getQuestionImageUrl();
        this.questionType = question.getQuestionType();
        this.timeLimitSeconds = question.getTimeLimitSeconds();
        this.points = question.getPoints();
        this.isActive = question.getIsActive();
        this.answerOptions = answerOptions;
    }
}