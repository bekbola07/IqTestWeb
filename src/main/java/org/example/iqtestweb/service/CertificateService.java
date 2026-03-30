package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.AuthorCertificate;
import org.example.iqtestweb.entity.Quiz;
import org.example.iqtestweb.entity.UserCertificate;
import org.example.iqtestweb.entity.TestSession;
import org.example.iqtestweb.repository.AuthorCertificateRepository;
import org.example.iqtestweb.repository.UserCertificateRepository;
import org.example.iqtestweb.repository.TestSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final UserCertificateRepository userCertificateRepository;
    private final AuthorCertificateRepository authorCertificateRepository;
    private final TestSessionRepository testSessionRepository;
    private final PdfGenerationService pdfGenerationService;
    private final QrCodeService qrCodeService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 4;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public UserCertificate generateCertificate(Long sessionId, String displayName) {
        TestSession session = testSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Test session not found"));

        if (userCertificateRepository.findByTestSession_SessionId(sessionId).isPresent()) {
            return userCertificateRepository.findByTestSession_SessionId(sessionId).get();
        }

        if (!session.getQuizSnapshot().getQuiz().isCertificateEnabled()) {
            throw new IllegalStateException("Certificates are not enabled for this quiz");
        }

        AuthorCertificate authorCertificate = authorCertificateRepository.findByQuizId(session.getQuizSnapshot().getQuiz().getId())
                .orElseGet(() -> {
                    // Fallback: create a default author certificate for existing quizzes
                    Quiz quiz = session.getQuizSnapshot().getQuiz();
                    AuthorCertificate newCert = AuthorCertificate.builder()
                            .quiz(quiz)
                            .title(quiz.getName() + " Certificate")
                            .description("Certificate of Completion")
                            .createdBy(quiz.getUser())
                            .templatePath("certificate/template")
                            .build();
                    return authorCertificateRepository.save(newCert);
                });

        if (authorCertificate.getPassingScore() != null && session.getIqScore() < authorCertificate.getPassingScore()) {
             throw new IllegalStateException("Score is below passing score for certificate");
        }

        UserCertificate certificate = UserCertificate.builder()
                .authorCertificate(authorCertificate)
                .assessedUser(session.getUser())
                .testSession(session)
                .certificateName(displayName != null ? displayName : session.getUser().getUsername())
                .verificationCode(generateUniqueCode())
                .build();

        return userCertificateRepository.save(certificate);
    }

    public Optional<UserCertificate> getUserCertificateBySessionId(Long sessionId) {
        return userCertificateRepository.findByTestSession_SessionId(sessionId);
    }

    @Transactional
    public UserCertificate getOrCreateCertificate(Long sessionId) {
        return userCertificateRepository.findByTestSession_SessionId(sessionId)
                .orElseGet(() -> {
                    TestSession session = testSessionRepository.findById(sessionId)
                            .orElseThrow(() -> new IllegalArgumentException("Test session not found"));
                    return generateCertificate(sessionId, session.getUser().getUsername());
                });
    }

    @Transactional
    public UserCertificate updateCertificateName(Long sessionId, String newName) {
        UserCertificate certificate = userCertificateRepository.findByTestSession_SessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found"));
        
        certificate.setCertificateName(newName);
        return userCertificateRepository.save(certificate);
    }

    @Transactional
    public byte[] downloadCertificatePdf(Long sessionId) {
        // Use findByTestSession_SessionId to ensure we get the latest state from DB
        UserCertificate certificate = userCertificateRepository.findByTestSession_SessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found for this session"));

        certificate.setDownloaded(true);
        certificate.setLastDownloadedAt(LocalDateTime.now());
        certificate.setCountDownloads(certificate.getCountDownloads() + 1);
        userCertificateRepository.save(certificate);

        Map<String, Object> variables = new HashMap<>();
        variables.put("certificateName", certificate.getAuthorCertificate().getTitle());
        variables.put("userFullName", certificate.getCertificateName());
        variables.put("score", certificate.getTestSession().getIqScore());
        variables.put("assessmentCreator", certificate.getAuthorCertificate().getCreatedBy().getUsername());
        variables.put("verificationCode", certificate.getVerificationCode());
        variables.put("issuedDate", certificate.getAssessmentDate());
        
        String verificationUrl = baseUrl + "/cert/verify/" + certificate.getVerificationCode();
        variables.put("qrCodeBase64", qrCodeService.generateBase64(verificationUrl));

        String templatePath = certificate.getAuthorCertificate().getTemplatePath();
        if (templatePath == null || templatePath.isEmpty()) {
            templatePath = "certificate/template";
        }

        return pdfGenerationService.generatePdfFromTemplate(templatePath, variables);
    }

    public UserCertificate verifyCertificate(String verificationCode) {
        UserCertificate certificate = userCertificateRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification code"));
        
        certificate.setVerified(true);
        certificate.setLastVerifiedAt(LocalDateTime.now());
        certificate.setCountVerification(certificate.getCountVerification() + 1);
        userCertificateRepository.save(certificate);
        
        return certificate;
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = String.format("VER-%s-%s-%d", 
                generateRandomString(CODE_LENGTH), 
                generateRandomString(CODE_LENGTH), 
                Year.now().getValue());
        } while (userCertificateRepository.existsByVerificationCode(code));
        return code;
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
