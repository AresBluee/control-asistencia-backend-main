package com.agro.control_asistencia_backend.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender; 

    public void sendEmail(String to, String subject, String body) {
        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("ares19951208@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true indica que es HTML
            
            mailSender.send(message);
            System.out.println("✅ Correo HTML enviado con éxito a: " + to);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar el correo HTML a " + to + ": " + e.getMessage());
        }
    }
}
