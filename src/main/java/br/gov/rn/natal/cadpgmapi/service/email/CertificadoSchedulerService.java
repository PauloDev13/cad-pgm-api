package br.gov.rn.natal.cadpgmapi.service.email;

import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.service.ProcuradorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CertificadoSchedulerService {
    private final ProcuradorService procuradorService;
    private final EmailService emailService;

    public CertificadoSchedulerService(ProcuradorService procuradorService, EmailService emailService) {
        this.procuradorService = procuradorService;
        this.emailService = emailService;
    }

    // Para teste local imediato (dispara a cada 1 minuto), descomente a linha abaixo e comente a de produção:
     @Scheduled(cron = "0 * * * * *")

    // Expressão de Produção: Executa toda segunda-feira às 08:00 AM
//    @Scheduled(cron = "0 0 8 * * MON")
    public void agendarVerificacaoCertificados() {
        log.info("Iniciando rotina agendada: Verificação de vencimento de certificados...");

        List<ProcuradorResponseDTO> listaAVencer = procuradorService.listarCertificadosProximosDoVencimento();

        if (listaAVencer != null && !listaAVencer.isEmpty()) {
            emailService.enviarEmailCertificadosAVencer(listaAVencer);
            log.info("Sucesso: E-mail de notificação enviado contendo {} certificados a vencer.", listaAVencer.size());
        } else {
            log.info("Nenhum certificado próximo do vencimento encontrado na janela atual. E-mail ignorado.");
        }
    }
}
