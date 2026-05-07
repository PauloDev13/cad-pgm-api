package br.gov.rn.natal.cadpgmapi.audit.aspects;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.entities.AuditLog;
import br.gov.rn.natal.cadpgmapi.audit.events.AuditLogEvent;
import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
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

import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;

@Aspect
@Component
public class AuditAspect {

    private final ApplicationEventPublisher eventPublisher;

    public AuditAspect(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

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

                if (authentication.getDetails() instanceof DecodedJWT jwt) {
                    Date iat = jwt.getIssuedAt();
                    if (iat != null) {
                        dateHourLogin = iat.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    }
                }
            }

            // 3. Descobre o ID do registro afetado (Assume que a entidade/DTO retornado tem um método id())
            String affectedId = "N/A";

            if (result != null) {
                affectedId = extractFriendlyId(result);

                // Fallback para métodos VOID (como o Delete, que só recebe o ID por parâmetro)
                if (affectedId.equals("N/A") && joinPoint.getArgs().length > 0) {
                    Object firstArgument = joinPoint.getArgs()[0];
                    if (firstArgument != null) {
                        affectedId = firstArgument.toString();
                    }
                }
            }

            // Fallback para métodos VOID (como o Delete)
            // Lê o primeiro argumento passado para o método (que será o ID)
            if (affectedId.equals("N/A") && joinPoint.getArgs().length > 0) {
                Object firstArgument = joinPoint.getArgs()[0];

                if (firstArgument != null) {
                    affectedId = firstArgument.toString();
                }
            }

            String entityName = auditable.entity();

            // Se a anotação não informou a entidade (@Auditable(action = INSERT))
            if (entityName == null || entityName.isBlank()) {

                // Pega a classe real em tempo de execução (ex: ServidorService)
                Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());

                // Sobe para a superclasse genérica (BaseService) e extrai o tipo <T>
                Class<?> entityClass = ResolvableType.forClass(targetClass)
                        .getSuperType()
                        .resolveGeneric(0); // 0 = Pega o primeiro genérico. Ex: <Servidor, Long> pega Servidor.

                entityName = entityClass.getSimpleName(); // Retorna "Servidor"

                if (result != null) {
                    // Fallback de segurança: limpa o sufixo DTO caso ResolvableType falhe
                    entityName = result.getClass().getSimpleName()
                            .replace("ResponseDTO", "")
                            .replace("RequestDTO", "")
                            .replace("DTO", "");
                } else {
                    entityName = "Unknown";
                }
            }

            // 4. Monta o log
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
                        log.setDetails(entityName + " ID " + affectedId + " criado(a) com sucesso.");
                        break;
                    case DELETE:
                        log.setDetails(entityName + " ID " + affectedId + " excluído(a) com sucesso.");
                        break;
                    default:
                        log.setDetails("Método executado: " + method.getName());
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

    // MÉTODOS PRIVADOS
    /**
     * Tenta extrair o atributo anotado com @AuditFriendlyId.
     * Se não encontrar, faz o fallback para o método id() padrão.
     */
    private String extractFriendlyId(Object result) {
        try {
            // 1. Procura a anotação nos campos (Fields)
            for (java.lang.reflect.Field field : result.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(AuditFriendlyId.class)) {
                    field.setAccessible(true);
                    Object value = field.get(result);
                    if (value != null && !value.toString().isBlank()) return value.toString();
                }
            }

            // 2. Procura a anotação nos métodos (útil para Records no Java)
            for (Method method : result.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(AuditFriendlyId.class)) {
                    Object value = method.invoke(result);
                    if (value != null && !value.toString().isBlank()) return value.toString();
                }
            }

            // 3. Fallback: Se não tem a etiqueta, usa o "id()" como antigamente
            Method getIdMethod = result.getClass().getMethod("id");
            Object idValue = getIdMethod.invoke(result);
            if (idValue != null) return idValue.toString();

        } catch (Exception e) {
            // Ignora silenciosamente e retorna N/A
        }

        return "N/A";
    }
}