package org.example.iqtestweb.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizDurationDto {
    private Integer minutes = 0;

    public Integer toTotalSeconds() {
        return minutes != null ? minutes * 60 : 0;
    }

    public static QuizDurationDto fromTotalSeconds(Integer totalSeconds) {
        if (totalSeconds == null || totalSeconds <= 0) {
            return new QuizDurationDto(0);
        }
        return new QuizDurationDto(totalSeconds / 60);
    }
}
