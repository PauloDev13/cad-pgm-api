package br.gov.rn.natal.cadpgmapi.audit.aspects;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.entities.AuditLog;
import br.gov.rn.natal.cadpgmapi.audit.events.AuditLogEvent;
import br.gov.rn.natal.cadpgmapi.audit.utils.AuditDiffUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Aspect
@Component
public class AuditAspect {

    // DEFICIÊNCIA CORRIGIDA (b5.4/b4.18): a versão anterior imprimia os erros com
    // System.err.println (sem timestamps/nível) e mantinha trechos de código morto
    // (extração de assinatura comentada; cálculo de 'entityClass' que nunca era usado;
    // imports de AopUtils/ResolvableType). Agora usamos um logger SLF4J padrão.
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final ApplicationEventPublisher eventPublisher;

    public AuditAspect(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @AfterReturning(value = "@annotation(auditable)", returning = "result")
    public void logAuditActivity(JoinPoint joinPoint, Object result, Auditable auditable) {
        try {
            // 1. Usuário logado + data/hora do login (extraída do JWT guardado nos "details")
            SessionInfo sessao = extractSessionInfo();

            // 2. ID amigável: prioridade para o valor injetado no contexto (ex.: delete),
            //    depois extração do resultado (insert/update) e, por fim, o primeiro argumento.
            String affectedId = extractAffectedId(result, joinPoint);

            // 3. Nome da entidade auditada (contexto > anotação > classe do resultado)
            String entityName = extractEntityName(auditable, result);

            // 4. Monta o log e publica o evento (o listener grava em thread separada)
            AuditLog auditLog = buildAuditLog(
                    joinPoint, auditable, sessao, entityName, affectedId
            );
            eventPublisher.publishEvent(new AuditLogEvent(this, auditLog));

        } catch (Exception e) {
            log.error("Falha ao gerar log de auditoria", e);
        } finally {
            // Sempre limpa o ThreadLocal ao final (sucesso OU erro)
            AuditContextHolder.clear();
        }
    }

    /**
     * CORREÇÃO DE BUG (vazamento de ThreadLocal):
     * O advice acima usa @AfterReturning, que SÓ roda quando o método anotado termina SEM
     * exceção. Quando uma BusinessException era lançada (ex.: validação de CPF duplicado no
     * beforeCreate), o finally do advice acima nem chegava a rodar e o AuditContextHolder
     * permanecia "sujo". Como o Tomcat reutiliza threads de um POOL, os dados do usuário
     * anterior vazavam para a PRÓXIMA requisição — contaminação entre usuários + memory leak.
     * Este advice roda quando o método auditado LANÇA qualquer exceção e garante a limpeza.
     */
    @AfterThrowing(value = "@annotation(auditable)", throwing = "error")
    public void clearAuditContextOnError(Auditable auditable, Throwable error) {
        AuditContextHolder.clear();
    }

    // ---------------- Helpers de leitura (extraídos para legibilidade - b5.11) ----------------

    /** Guarda o usuário + data/hora de login extraídos do JWT. */
    private record SessionInfo(String username, LocalDateTime dateHourLogin) {}

    private SessionInfo extractSessionInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "SISTEMA";
        LocalDateTime dateHourLogin = null;

        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();

            if (authentication.getDetails() instanceof DecodedJWT jwt) {
                Date iat = jwt.getIssuedAt();
                if (iat != null) {
                    dateHourLogin = iat.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                }
            }
        }
        return new SessionInfo(username, dateHourLogin);
    }

    private String extractAffectedId(Object result, JoinPoint joinPoint) {
        String affectedId = AuditContextHolder.getFriendlyId();

        if (affectedId != null && !affectedId.equals("N/A")) {
            return affectedId;
        }

        // Sem valor no contexto (ex.: INSERT), tenta extrair do resultado retornado
        if (result != null) {
            return AuditDiffUtil.extractFriendlyId(result);
        }

        // Fallback: primeiro argumento (deve ser um ID simples). Pula argumentos "complicados"
        // (DTOs, MultipartFile etc.) que não são uma identificação legível de registros.
        if (joinPoint.getArgs().length > 0) {
            Object firstArg = joinPoint.getArgs()[0];
            if (firstArg instanceof Number || firstArg instanceof String) {
                return firstArg.toString();
            }
        }

        return "N/A";
    }

    private String extractEntityName(Auditable auditable, Object result) {
        // Prioridade 1: nome setado no contexto (feito no delete do BaseGenericService)
        String entityName = AuditContextHolder.getEntityName();
        if (entityName != null && !entityName.isBlank()) {
            return entityName;
        }

        // Prioridade 2: o valor explícito da anotação @Auditable(entity = "...")
        if (auditable != null && auditable.entity() != null && !auditable.entity().isBlank()) {
            return auditable.entity();
        }

        // Prioridade 3: infere da classe do retorno (ex.: ServidorResponseDTO -> Servidor)
        if (result != null) {
            return result.getClass().getSimpleName().replace("ResponseDTO", "");
        }

        return "Unknown";
    }

    private AuditLog buildAuditLog(
            JoinPoint joinPoint,
            Auditable auditable,
            SessionInfo sessao,
            String entityName,
            String affectedId
    ) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(sessao.username());
        auditLog.setDateHourLogin(sessao.dateHourLogin());
        auditLog.setDateHourAction(LocalDateTime.now());
        auditLog.setTypeAction(auditable.action());
        auditLog.setAffectedEntity(entityName);
        auditLog.setIdAffectedRecord(affectedId);

        String extraDetails = AuditContextHolder.getLogDetalhes();

        if (extraDetails != null && !extraDetails.isBlank()) {
            auditLog.setDetails(extraDetails);
        } else {
            switch (auditable.action()) {
                case INSERT -> auditLog.setDetails("INCLUSÃO: " + affectedId + " criado(a) com sucesso.");
                case DELETE -> auditLog.setDetails("EXCLUSÃO: " + affectedId + " excluído(a) com sucesso.");
                default -> auditLog.setDetails("Método executado: " + joinPoint.getSignature().getName());
            }
        }
        return auditLog;
    }
}