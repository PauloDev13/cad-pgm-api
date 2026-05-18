package br.gov.rn.natal.cadpgmapi.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmailRecovery(String recipient, String link) {
        try {
            // 1. Cria a mensagem MIME (que suporta HTML)
            MimeMessage message = mailSender.createMimeMessage();

            // 2. O Helper facilita a montagem do e-mail. O "true" indica multipart, e setamos o padrão UTF-8 para acentuação.
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remetente);
            helper.setTo(recipient);
            helper.setSubject("Recuperação de Senha - PGM TI");

            // 3. Monta o corpo do e-mail em HTML com o botão estilizado
            String htmlMsg = "<div style='font-family: Arial, sans-serif; color: #333333; line-height: 1.6; max-width: 600px; margin: 0 auto;'>"
                    + "<div style='text-align: left; margin-bottom: 25px;'>"
                    + "  <img src='cid:logoPgm' alt='Logomarca PGM' style='max-width: 250px; height: auto;' />"
                    + "</div>"
                    + "<h2>Recuperação de Senha</h2>"
                    + "<p>Você solicitou a recuperação da sua senha.</p>"
                    + "<p>Clique no botão abaixo para redefinir sua senha de acesso:</p>"
                    + "<div style='margin: 25px 0;'>"
                    + "  <a href='" + link + "' "
                    + "     style='background-color: #007BFF; color: #FFFFFF; padding: 12px 25px; "
                    + "            text-decoration: none; border-radius: 5px; font-weight: bold; "
                    + "            display: inline-block; font-size: 16px;'>"
                    + "     Redefinir Senha"
                    + "  </a>"
                    + "</div>"
                    + "<p>Este link é válido por <strong>30 minutos</strong>.</p>"
                    + "<p style='font-size: 12px; color: #777777; border-top: 1px solid #DDDDDD; padding-top: 15px;'>"
                    + "Se você não solicitou a alteração de senha, por favor, ignore este e-mail. "
                    + "Sua senha atual permanecerá inalterada.</p>"
                    + "</div>";

            // 4. Seta o texto. O segundo parâmetro "true" avisa o Spring que o conteúdo é HTML!
            helper.setText(htmlMsg, true);

            // O addInline DEVE ser chamado SEMPRE DEPOIS do setText!
            // O ClassPathResource mapeia automaticamente a partir da pasta src/main/resources/
            ClassPathResource logoImage = new ClassPathResource("images/logo.png");
            helper.addInline("logoPgm", logoImage);

            // 6. Dispara o e-mail
            mailSender.send(message);

        } catch (MessagingException e) {
            // Como o MimeMessageHelper exige tratamento de exceção, nós encapsulamos e lançamos
            // para não quebrar a assinatura original do seu método.
            throw new RuntimeException("Falha ao montar o e-mail HTML de recuperação", e);
        }
    }
}