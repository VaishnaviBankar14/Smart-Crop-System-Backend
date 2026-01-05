package com.smartcrops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetLink(String toEmail, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset Password - Smart Crop System");
        message.setText(
            "Click the link below to reset your password:\n\n" +
            resetLink +
            "\n\nThis link is valid for 15 minutes."
        );

        mailSender.send(message);
    }
}
