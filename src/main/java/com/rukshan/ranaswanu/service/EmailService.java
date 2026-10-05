package com.rukshan.ranaswanu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset your Ranaswanu password");
        message.setText("Click the link below to reset your password:\n\n" + resetLink +
                "\n\nThis link expires in 30 minutes. If you didn't request this, ignore this email.");

        mailSender.send(message);
    }
}