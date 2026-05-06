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
        SimpleMailMessage message = new SimpleMailMessage();
        
        // El remitente debe ser el mismo que configuraste en application.properties
        message.setFrom("ares19951208gmail.com"); 
        
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        
        try {
            // 🚀 ESTO ES EL ENVÍO REAL
            mailSender.send(message); 
            System.out.println("✅ Correo enviado con éxito a: " + to);
        } catch (Exception e) {
            System.err.println("❌ Error al enviar el correo a " + to + ": " + e.getMessage());
            // Idealmente, aquí deberías lanzar una excepción personalizada o loguear el error.
        }
    }
}
