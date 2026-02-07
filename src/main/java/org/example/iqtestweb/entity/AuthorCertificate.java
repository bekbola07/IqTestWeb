package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "author_certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false, unique = true)
    private Quiz quiz;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "template_path")
    private String templatePath; // e.g., "templates/certificates/modern.html"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "passing_score")
    private Integer passingScore; // Minimum score to get the certificate

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
