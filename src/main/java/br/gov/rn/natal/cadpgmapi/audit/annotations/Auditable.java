package br.gov.rn.natal.cadpgmapi.audit.annotations;

import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // Só pode ser usada em métodos
@Retention(RetentionPolicy.RUNTIME) // Fica disponível enquanto a aplicação roda
public @interface Auditable {
    AuditAction action();
    String entity() default "";
}
