package com.hardik.casino.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@casino-platform.local}")
    private String fromAddress;

    public void sendPasswordResetEmail(String toEmail, String username, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Reset your password");
        message.setText(
                "Hello " + username + ",\n\n" +
                        "Use the link below to reset your password:\n" +
                        resetLink + "\n\n" +
                        "If you did not request this change, you can ignore this email."
        );
        mailSender.send(message);
    }
}
