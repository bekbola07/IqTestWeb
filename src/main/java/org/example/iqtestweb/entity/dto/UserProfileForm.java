package org.example.iqtestweb.entity.dto;

import lombok.Data;
import org.example.iqtestweb.entity.enums.AcademicDegree;
import org.example.iqtestweb.entity.enums.FieldOfActivity;

@Data
public class UserProfileForm {
    private Integer age;
    private AcademicDegree academicDegree;
    private FieldOfActivity fieldOfActivity;
    private String country;
}
