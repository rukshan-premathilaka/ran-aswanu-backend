package com.rukshan.ranaswanu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    // Must be the Gmail address that owns the app password (Gmail rejects or spam-filters other senders)
    @Value("${app.mail.from}")
    private String from;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from.contains("<") ? from : "Ranaswanu <" + from + ">");
        message.setTo(toEmail);
        message.setSubject("Reset your Ranaswanu password");
        message.setText("Click the link below to reset your password:\n\n" + resetLink +
                "\n\nThis link expires in 30 minutes. If you didn't request this, ignore this email.");

        try {
            mailSender.send(message);
            log.info("Password reset email sent to {}", mask(toEmail));
        } catch (MailException ex) {
            // Do not fail the request (and do not reveal which emails exist). The real reason is in
            // the logs: run  heroku logs --tail  and look for "Password reset email FAILED".
            Throwable root = ex;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            log.error("Password reset email FAILED for {} - {}: {}", mask(toEmail),
                    ex.getClass().getSimpleName(), root.getMessage(), ex);
        }
    }

    private String mask(String email) {
        int at = email.indexOf('@');
        return at > 1 ? email.charAt(0) + "***" + email.substring(at) : "***";
    }
}
