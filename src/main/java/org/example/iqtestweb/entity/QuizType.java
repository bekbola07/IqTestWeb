package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.iqtestweb.entity.enums.Status;

@Entity
@Table(name = "quiz_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @PreRemove
    public void preRemove() {
        this.status = Status.DELETED;
    }
}
