package com.smartcrops.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${SENDGRID_API_KEY}")
    private String sendGridApiKey;

    public void sendResetLink(String toEmail, String resetLink) {

        Email from = new Email("smartcropsystem14@gmail.com"); // verified sender
        String subject = "Reset Password - Smart Crop System";
        Email to = new Email(toEmail);

        Content content = new Content(
            "text/plain",
            "Click the link below to reset your password:\n\n"
            + resetLink +
            "\n\nThis link is valid for 15 minutes."
        );

        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send email via SendGrid", e);
        }
    }
}
