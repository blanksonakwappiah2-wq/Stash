package com.quickbite.backend.services;

import com.quickbite.backend.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Async
    public void sendLoginNotification(User user) {
        if (mailSender == null) {
            System.out.println("[WARN] EmailService: JavaMailSender not configured. Skipping email to " + user.getEmail());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@quickbite.com");
            message.setTo(user.getEmail());
            message.setSubject("Welcome back to QuickBite!");
            message.setText("Hello " + user.getName() + ",\n\n" +
                    "Welcome back to QuickBite! We're glad to see you again.\n\n" +
                    "If this wasn't you, please secure your account immediately.\n\n" +
                    "Happy dining,\nThe QuickBite Team");

            mailSender.send(message);
            System.out.println("[INFO] Login notification sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to send login notification to " + user.getEmail() + ": " + e.getMessage());
        }
    }
}
