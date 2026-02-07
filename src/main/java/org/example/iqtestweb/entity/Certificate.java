package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "certificates", indexes = {
    @Index(name = "idx_certificate_code", columnList = "verification_code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Format: VER-XXXX-XXXX-YEAR (e.g., VER-9F3A-82KD-2026)
    @Column(name = "verification_code", nullable = false, unique = true, length = 32)
    private String verificationCode;

    @Column(name = "certificate_name", nullable = false)
    private String certificateName;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    // The user who took the test
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessed_user_id", nullable = false)
    private User assessedUser;

    // Manual name input (e.g., "John Doe" instead of "johndoe123")
    @Column(name = "assessed_user_display_name", nullable = false)
    private String assessedUserDisplayName;

    // The creator of the quiz (Organization/Admin)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_owner_id", nullable = false)
    private User assessmentOwner;

    // Link to the specific attempt (One-to-One ensures only one cert per attempt)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_session_id", nullable = false, unique = true)
    private TestSession testSession;
}
