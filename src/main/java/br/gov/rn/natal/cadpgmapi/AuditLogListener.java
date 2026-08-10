package br.gov.rn.natal.cadpgmapi;

import br.gov.rn.natal.cadpgmapi.audit.events.AuditLogEvent;
import br.gov.rn.natal.cadpgmapi.audit.repositories.AuditLogRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuditLogListener {

    private final AuditLogRepository repository;

    public AuditLogListener(AuditLogRepository repository) {
        this.repository = repository;
    }

    // O @Async("asyncExecutor") faz este método rodar em uma thread SEPARADA, usando o pool
    // definido no AsyncConfig (que ativa o @EnableAsync — antes essa anotação não surtia efeito).
    // Por rodar FORA da transação do método auditado, o log de auditoria continua sendo
    // gravado MESMO que a operação de negócio faça rollback (requisito de design).
    @Async("asyncExecutor")
    @EventListener
    public void handleAuditLogEvent(AuditLogEvent event) {
        repository.save(event.getAuditLog());
    }
}
