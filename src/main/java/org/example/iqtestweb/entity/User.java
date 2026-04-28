package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.iqtestweb.entity.enums.AcademicDegree;
import org.example.iqtestweb.entity.enums.FieldOfActivity;
import org.example.iqtestweb.entity.enums.Status;
import org.example.iqtestweb.entity.enums.UserRole;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"testSessions"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true, nullable = false)
    @Email
    private String email;

    @Column(unique = true, nullable = false)
    @Size(min = 3, max = 50)
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    private Integer age;
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_degree")
    private AcademicDegree academicDegree;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_of_activity")
    private FieldOfActivity fieldOfActivity;

    @Column(name = "oauth_provider")
    private String oauthProvider; // "google", "github", "local"

    @Column(name = "oauth_id")
    private String oauthId;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<TestSession> testSessions;

    @PreRemove
    public void preRemove() {
        this.status = Status.DELETED;
    }
}
