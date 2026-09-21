package br.gov.rn.natal.cadpgmapi.service.email;

import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
//import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    @Value("${app.mail.certificado.remetente}")
    private String remetenteCertificado;

    @Value("${app.mail.certificado.destinatario}")
    private String destinatarioCertificado;

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
            throw new BusinessException("Falha ao montar o e-mail HTML de recuperação", e);
        }
    }

    public void enviarEmailCertificadosAVencer(List<ProcuradorResponseDTO> procuradores) {
        if (procuradores == null || procuradores.isEmpty()) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(remetenteCertificado);
            helper.setTo(destinatarioCertificado);
            helper.setSubject("Aviso: Certificados Digitais Próximos do Vencimento - PGM");

            StringBuilder html = new StringBuilder();
            html.append("<div style='font-family: Arial, sans-serif; color: #333333; line-height: 1.6; max-width: 600px; margin: 0 auto;'>");
            html.append("<div style='text-align: left; margin-bottom: 25px;'>");
            html.append("  <img src='cid:logoPgm' alt='Logomarca PGM' style='max-width: 250px; height: auto;' />");
            html.append("</div>");
            html.append("<h2>Certificados Digitais a Expirar (7 dias ou menos)</h2>");
            html.append("<p>Abaixo encontra-se a lista de procuradores com certificados a necessitar de renovação:</p>");

            html.append("<table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>");
            html.append("<tr style='background-color: #f2f2f2;'>")
                    .append("<th style='border: 1px solid #dddddd; padding: 8px; text-align: left;'>Procurador</th>")
                    .append("<th style='border: 1px solid #dddddd; padding: 8px; text-align: left;'>Tipo</th>")
                    .append("<th style='border: 1px solid #dddddd; padding: 8px; text-align: left;'>Data de Expiração</th>")
                    .append("</tr>");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (ProcuradorResponseDTO p : procuradores) {
                String dataFormatada = p.dataExpiracao() != null ? p.dataExpiracao().format(formatter) : "N/D";
                html.append("<tr>")
                        .append("<td style='border: 1px solid #dddddd; padding: 8px;'>").append(p.nome()).append("</td>")
                        .append("<td style='border: 1px solid #dddddd; padding: 8px;'>").append(p.tipoCertificado()).append("</td>")
                        .append("<td style='border: 1px solid #dddddd; padding: 8px;'>").append(dataFormatada).append("</td>")
                        .append("</tr>");
            }
            html.append("</table>");
            html.append("<p style='font-size: 12px; color: #777777; border-top: 1px solid #DDDDDD; margin-top: 25px; padding-top: 15px;'>")
                    .append("Este é um e-mail automático gerado pelo sistema CAD PGM. Por favor, não responda.</p>");
            html.append("</div>");

            helper.setText(html.toString(), true);

            // Adicionar a logomarca da PGM como imagem embutida (inline)
            ClassPathResource logoImage = new ClassPathResource("images/logo.png");
            helper.addInline("logoPgm", logoImage);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new br.gov.rn.natal.cadpgmapi.exception.BusinessException("Falha ao montar o e-mail HTML de certificados a vencer.", e);
        }
    }
}