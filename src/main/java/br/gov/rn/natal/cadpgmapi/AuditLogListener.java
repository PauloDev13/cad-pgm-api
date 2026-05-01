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

    // O @Async faz este método rodar em uma thread separada!
    @Async
    @EventListener
    public void handleAuditLogEvent(AuditLogEvent event) {
        repository.save(event.getAuditLog());
    }
}
