package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.Certificate;
import org.example.iqtestweb.entity.TestSession;
import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.repository.CertificateRepository;
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

    private final CertificateRepository certificateRepository;
    private final TestSessionRepository testSessionRepository;
    private final PdfGenerationService pdfGenerationService;
    private final QrCodeService qrCodeService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // No I, O, 1, 0 to avoid confusion
    private static final int CODE_LENGTH = 4;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public Certificate generateCertificate(Long sessionId, String displayName) {
        TestSession session = testSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Test session not found"));

        if (certificateRepository.findByTestSessionSessionId(sessionId).isPresent()) {
            return certificateRepository.findByTestSessionSessionId(sessionId).get();
        }

        if (!session.getQuizSnapshot().getQuiz().isCertificateEnabled()) {
            throw new IllegalStateException("Certificates are not enabled for this quiz");
        }

        Certificate certificate = new Certificate();
        certificate.setVerificationCode(generateUniqueCode());
        certificate.setCertificateName(session.getQuizSnapshot().getTitle() + " Certificate");
        certificate.setScore(session.getIqScore()); // Assuming IQ score is what we want to display
        certificate.setIssuedAt(LocalDateTime.now());
        certificate.setAssessedUser(session.getUser());
        certificate.setAssessedUserDisplayName(displayName);
        certificate.setAssessmentOwner(session.getQuizSnapshot().getQuiz().getUser());
        certificate.setTestSession(session);

        return certificateRepository.save(certificate);
    }

    public Optional<Certificate> getCertificateBySessionId(Long sessionId) {
        return certificateRepository.findByTestSessionSessionId(sessionId);
    }

    public byte[] downloadCertificatePdf(Long sessionId) {
        Certificate certificate = certificateRepository.findByTestSessionSessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found for this session"));

        Map<String, Object> variables = new HashMap<>();
        variables.put("certificateName", certificate.getCertificateName());
        variables.put("userFullName", certificate.getAssessedUserDisplayName());
        variables.put("score", certificate.getScore());
        variables.put("assessmentCreator", certificate.getAssessmentOwner().getUsername()); // Or a display name if available
        variables.put("verificationCode", certificate.getVerificationCode());
        variables.put("issuedDate", certificate.getIssuedAt());
        
        String verificationUrl = baseUrl + "/cert/verify/" + certificate.getVerificationCode();
        variables.put("qrCodeBase64", qrCodeService.generateBase64(verificationUrl));

        return pdfGenerationService.generatePdfFromTemplate("certificate/template", variables);
    }

    public Certificate verifyCertificate(String verificationCode) {
        return certificateRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification code"));
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = String.format("VER-%s-%s-%d", 
                generateRandomString(CODE_LENGTH), 
                generateRandomString(CODE_LENGTH), 
                Year.now().getValue());
        } while (certificateRepository.existsByVerificationCode(code));
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
