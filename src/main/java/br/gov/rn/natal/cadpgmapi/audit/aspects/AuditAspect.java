package br.gov.rn.natal.cadpgmapi.audit.aspects;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.entities.AuditLog;
import br.gov.rn.natal.cadpgmapi.audit.events.AuditLogEvent;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Aspect
@Component
public class AuditAspect {

    private final ApplicationEventPublisher eventPublisher;

    public AuditAspect(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    // Intercepta métodos anotados com @Auditable APÓS retornarem com sucesso.
    // O parâmetro 'result' captura o objeto que o método salvou/retornou.
    @AfterReturning(value = "@annotation(br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable)", returning = "result")
    public void logAuditActivity(JoinPoint joinPoint, Object result) {
        try {
            // 1. Extrai a anotação para saber a ação e a entidade
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Auditable auditable = method.getAnnotation(Auditable.class);

            // 2. Extrai dados do usuário logado via SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = "SISTEMA";
            LocalDateTime dateHourLogin = null;

            if (authentication != null && authentication.isAuthenticated()) {
                username = authentication.getName();

                // Se você estiver guardando o DecodedJWT nas credentials:
                if (authentication.getDetails() instanceof DecodedJWT jwt) {
                    Date iat = jwt.getIssuedAt();
                    System.out.println("IAT JWT " + iat);
                    if (iat != null) {
                        dateHourLogin = iat.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    }
                }
            }

            // 3. Descobre o ID do registro afetado (Assume que a entidade retornada tem um método getId())
            String AffectedId = "N/A";

            if (result != null) {
                try {
                    Method getIdMethod = result.getClass().getMethod("id");
                    Object idValue = getIdMethod.invoke(result);
                    if (idValue != null) AffectedId = idValue.toString();
                } catch (Exception e) {
                    // Ignora se o objeto retornado não tiver getId()
                }
            }

            String entityName = auditable.entity();

            if (entityName.isEmpty() && result != null) {
                entityName = result.getClass().getSimpleName();
            } else if (entityName.isEmpty()) {
                entityName = "Unknown";
            }

            // 4. Monta o log
            AuditLog log = new AuditLog();
            log.setUsername(username);
            log.setDateHourLogin(dateHourLogin);
            log.setDateHourAction(LocalDateTime.now());
            log.setTypeAction(auditable.action());
            log.setAffectedEntity(entityName);
            log.setIdAffectedRecord(AffectedId);

            String extraDetails = AuditContextHolder.getLogDetalhes();

            if (extraDetails != null && !extraDetails.isBlank()) {
                log.setDetails(extraDetails);
            } else {
                log.setDetails("Método executado: " + method.getName());
            }

            // 5. Publica o evento (Síncrono aqui, mas processado Assíncrono pelo Listener)
            eventPublisher.publishEvent(new AuditLogEvent(this, log));

        } catch (Exception e) {
            // O Catch garante que um erro na auditoria NUNCA quebre a transação principal
            System.err.println("Falha ao gerar log de auditoria: " + e.getMessage());
        } finally {
            AuditContextHolder.clear();
        }
    }
}