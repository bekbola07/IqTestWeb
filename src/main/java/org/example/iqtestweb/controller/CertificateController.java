package org.example.iqtestweb.controller;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.UserCertificate;
import org.example.iqtestweb.service.CertificateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping("/api/certificates/download/{sessionId}")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long sessionId) {
        byte[] pdfBytes = certificateService.downloadCertificatePdf(sessionId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate.pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/cert/verify/{verificationCode}")
    public String verifyCertificate(@PathVariable String verificationCode, Model model) {
        try {
            UserCertificate certificate = certificateService.verifyCertificate(verificationCode);
            model.addAttribute("valid", true);
            model.addAttribute("certificate", certificate);
        } catch (IllegalArgumentException e) {
            model.addAttribute("valid", false);
        }
        return "certificate/verify";
    }
    
    @PostMapping("/api/certificates/generate/{sessionId}")
    @ResponseBody
    public ResponseEntity<String> generateCertificate(@PathVariable Long sessionId, @RequestParam String displayName) {
        certificateService.generateCertificate(sessionId, displayName);
        return ResponseEntity.ok("Certificate generated successfully");
    }

    @PostMapping("/api/certificates/update-name/{sessionId}")
    @ResponseBody
    public ResponseEntity<String> updateCertificateName(@PathVariable Long sessionId, @RequestParam String newName) {
        certificateService.updateCertificateName(sessionId, newName);
        return ResponseEntity.ok("Certificate name updated successfully");
    }

    @GetMapping("/certificate/claim/{sessionId}")
    public String claimCertificatePage(@PathVariable Long sessionId, Model model) {
        try {
             UserCertificate certificate = certificateService.getOrCreateCertificate(sessionId);
             model.addAttribute("certificate", certificate);
             return "certificate/claim";
        } catch (Exception e) {
            // Redirect with a user-friendly error message
            return "redirect:/quiz/result/" + sessionId + "?error=certificate_not_available";
        }
    }
}
