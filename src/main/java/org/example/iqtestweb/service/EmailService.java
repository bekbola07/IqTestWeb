package org.example.iqtestweb.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendHtmlMessage(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
    
    public String buildVerificationEmail(String userName, String verificationLink, String verificationCode) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head>" +
               "<body style=\"margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f9;\">" +
               "<table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr><td align=\"center\" style=\"padding: 40px 0;\">" +
               "<table role=\"presentation\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"600\" style=\"background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); overflow: hidden;\">" +
               "<tr><td style=\"background-color: #3498db; padding: 30px; text-align: center;\"><h1 style=\"color: #ffffff; margin: 0; font-size: 24px;\">Verify Your Account</h1></td></tr>" +
               "<tr><td style=\"padding: 40px 30px; line-height: 1.6; color: #333333;\">" +
               "<p style=\"font-size: 18px; margin-bottom: 20px;\">Hello <strong>" + userName + "</strong>,</p>" +
               "<p style=\"margin-bottom: 25px;\">Thank you for registering on <strong>IqTestWeb</strong>. To complete your signup and start your assessment, please verify your email address.</p>" +
               "<div style=\"text-align: center; margin-bottom: 30px;\">" +
               "<a href=\"" + verificationLink + "\" style=\"background-color: #3498db; color: #ffffff; padding: 15px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;\">Verify Account</a></div>" +
               "<p style=\"margin-bottom: 10px;\">Alternatively, you can enter this 6-digit code manually on the verification page:</p>" +
               "<div style=\"background-color: #f8f9fa; border: 1px dashed #3498db; padding: 15px; text-align: center; font-size: 28px; font-weight: bold; letter-spacing: 5px; color: #2c3e50; border-radius: 5px;\">" +
               verificationCode + "</div></td></tr>" +
               "<tr><td style=\"padding: 20px 30px; background-color: #fcfcfc; border-top: 1px solid #eeeeee; text-align: center; font-size: 12px; color: #999999;\">" +
               "<p style=\"margin: 0;\">This link and code will expire in 15 minutes.</p><p style=\"margin: 5px 0 0;\">If you did not create an account, please ignore this email.</p></td></tr>" +
               "</table></td></tr></table></body></html>";
    }
}
