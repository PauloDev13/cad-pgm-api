package br.gov.rn.natal.cadpgmapi.audit.aspects;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.entities.AuditLog;
import br.gov.rn.natal.cadpgmapi.audit.events.AuditLogEvent;
import br.gov.rn.natal.cadpgmapi.audit.utils.AuditDiffUtil;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;

@Aspect
@Component
public class AuditAspect {

    private final ApplicationEventPublisher eventPublisher;

    public AuditAspect(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @AfterReturning(value = "@annotation(auditable)", returning = "result")
    public void logAuditActivity(JoinPoint joinPoint, Object result, Auditable auditable) {
        try {
            // 1. Extrai a anotação para saber a ação e a entidade
//            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//            Method method = signature.getMethod();
//            Auditable auditable = method.getAnnotation(Auditable.class);

            // 1. Extrai dados do usuário logado via SecurityContext
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

            // 2. Descoberta do ID (Prioridade para o ID Amigável do Contexto)
            String affectedId = AuditContextHolder.getFriendlyId();

            if (affectedId == null || affectedId.equals("N/A")) {
                // Se não houver no contexto (ex: INSERT), tenta extrair do resultado
                if (result != null) {
                    affectedId = AuditDiffUtil.extractFriendlyId(result);
                } else if (joinPoint.getArgs().length > 0) {
                    // Fallback final para o primeiro argumento numérico
                    affectedId = joinPoint.getArgs()[0].toString();
                } else {
                    affectedId = "N/A";
                }
            }

            // 3. Nome vindo do Contexto (Setado no delete do BaseService)
            String entityName = AuditContextHolder.getEntityName();

            // 4. Se o contexto estiver vazio, tenta a anotação ou reflexão (para Insert/Update)
            if (entityName == null || entityName.isBlank()) {

                entityName = auditable.entity();

                if (entityName == null || entityName.isBlank()) {
                    // Pega a classe real em tempo de execução (ex: ServidorService)
                    Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());

                    // Sobe para a superclasse genérica (BaseService) e extrai o tipo <T>
                    Class<?> entityClass = ResolvableType.forClass(targetClass)
                            .as(BaseGenericService.class)
                            // 0 = Pega o primeiro genérico. Ex: <Servidor, Integer> pega Servidor.
                            .resolveGeneric(0);


                    entityName = entityClass.getSimpleName();

                    if (result != null) {
                        entityName = result.getClass().getSimpleName().replace("ResponseDTO", "");
                    } else {
                        entityName = "Unknown";
                    }
                }
            }

            // 5. Monta o log
            AuditLog log = new AuditLog();
            log.setUsername(username);
            log.setDateHourLogin(dateHourLogin);
            log.setDateHourAction(LocalDateTime.now());
            log.setTypeAction(auditable.action());
            log.setAffectedEntity(entityName);
            log.setIdAffectedRecord(affectedId);

            String extraDetails = AuditContextHolder.getLogDetalhes();

            if (extraDetails != null && !extraDetails.isBlank()) {
                log.setDetails(extraDetails);
            } else {
                // Mensagens automáticas inteligentes baseadas na ação
                switch (auditable.action()) {
                    case INSERT:
                        log.setDetails("INCLUSÃO " + affectedId + " criado(a) com sucesso.");
//                        log.setDetails(entityName + " ID: " + affectedId + " criado(a) com sucesso.");
                        break;
                    case DELETE:
                        log.setDetails("EXCLUSÃO: " + affectedId + " excluído(a) com sucesso.");
//                        log.setDetails(entityName + " ID: " + affectedId + " excluído(a) com sucesso.");
                        break;
                    default:
                        log.setDetails("Método executado: " + joinPoint.getSignature().getName());
                        break;
                }
            }

            // 5. Publica o evento
            eventPublisher.publishEvent(new AuditLogEvent(this, log));

        } catch (Exception e) {
            System.err.println("Falha ao gerar log de auditoria: " + e.getMessage());
        } finally {
            AuditContextHolder.clear();
        }
    }
}