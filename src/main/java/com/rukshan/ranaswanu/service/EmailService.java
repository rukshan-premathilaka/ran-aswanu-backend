package com.rukshan.ranaswanu.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    // Must be the Gmail address that owns the app password
    @Value("${app.mail.from}")
    private String from;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");

            String fromAddress = from.contains("<") ? from : "Ranaswanu <" + from + ">";
            helper.setFrom(fromAddress);
            helper.setReplyTo(from.contains("<") ? from.substring(from.indexOf('<') + 1, from.indexOf('>')) : from);
            helper.setTo(toEmail);
            helper.setSubject("Reset your Ranaswanu password");

            String text = "Hello,\n\n"
                    + "We received a request to reset the password of your Ranaswanu account.\n"
                    + "Open this link to choose a new password (valid for 30 minutes):\n\n"
                    + resetLink + "\n\n"
                    + "If you did not ask for this, you can ignore this email. Your password will not change.\n\n"
                    + "Ranaswanu";

            String safeLink = HtmlUtils.htmlEscape(resetLink);
            String html = "<div style=\"font-family:Arial,Helvetica,sans-serif;font-size:15px;color:#222;max-width:480px\">"
                    + "<p>Hello,</p>"
                    + "<p>We received a request to reset the password of your Ranaswanu account.</p>"
                    + "<p><a href=\"" + safeLink + "\" style=\"display:inline-block;background:#2e7d32;color:#fff;"
                    + "padding:10px 18px;border-radius:6px;text-decoration:none\">Choose a new password</a></p>"
                    + "<p>This link is valid for 30 minutes. If the button does not work, copy this address into your browser:<br>"
                    + "<span style=\"word-break:break-all\">" + safeLink + "</span></p>"
                    + "<p>If you did not ask for this, you can ignore this email. Your password will not change.</p>"
                    + "<p>Ranaswanu</p></div>";

            helper.setText(text, html); // plain-text part + HTML part (multipart/alternative)
            mime.setHeader("Auto-Submitted", "auto-generated");

            mailSender.send(mime);
            log.info("Password reset email sent to {}", mask(toEmail));
        } catch (MailException | jakarta.mail.MessagingException ex) {
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
