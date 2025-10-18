package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(unique = true)
    private String email;

    private String username;
    private Integer age;
    private String country;

    // OAuth fields
    @Column(name = "oauth_provider")
    private String oauthProvider; // "google", "github", "local"

    @Column(name = "oauth_id")
    private String oauthId;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<TestSession> testSessions;
}
