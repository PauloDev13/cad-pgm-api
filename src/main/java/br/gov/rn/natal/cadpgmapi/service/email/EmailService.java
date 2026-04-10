package br.gov.rn.natal.cadpgmapi.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailRecuperacao(String destinatario, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(remetente);
        message.setTo(destinatario);
        message.setSubject("Recuperação de Senha - PGM API");
        message.setText("Você solicitou a recuperação da sua senha.\n\n" +
                "Clique no link abaixo para redefinir sua senha:\n" + link +
                "\n\nEste link é válido por 30 minutos.\n" +
                "Se você não solicitou isso, apenas ignore este e-mail.");

        mailSender.send(message);
    }
}