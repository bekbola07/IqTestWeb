package org.example.iqtestweb.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_certificates", indexes = {
    @Index(name = "idx_user_cert_verification_code", columnList = "verification_code", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_certificate_id", nullable = false)
    private AuthorCertificate authorCertificate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessed_user_id", nullable = false)
    private User assessedUser;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_session_id", nullable = false, unique = true)
    private TestSession testSession;

    // Editable by user before first download
    @Column(name = "certificate_name", nullable = false)
    private String certificateName;

    @Column(name = "assessment_date", nullable = false)
    private LocalDateTime assessmentDate;

    // Format: VER-XXXX-XXXX-YEAR
    @Column(name = "verification_code", unique = true, length = 32)
    private String verificationCode;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "is_downloaded", nullable = false)
    private boolean isDownloaded = false;

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @Column(name = "last_downloaded_at")
    private LocalDateTime lastDownloadedAt;

    @Column(name = "count_verification", nullable = false)
    private int countVerification = 0;

    @Column(name = "count_downloads", nullable = false)
    private int countDownloads = 0;

    @PrePersist
    protected void onCreate() {
        assessmentDate = LocalDateTime.now();
    }
}
