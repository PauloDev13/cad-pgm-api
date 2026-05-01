package br.gov.rn.natal.cadpgmapi.audit.events;

import br.gov.rn.natal.cadpgmapi.audit.entities.AuditLog;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AuditLogEvent extends ApplicationEvent {
    private final AuditLog auditLog;

    public AuditLogEvent(Object source, AuditLog auditLog) {
        super(source);
        this.auditLog = auditLog;
    }

}