package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;

import java.util.Set;

public record UsuarioResponseDTO(
        Integer id,
        String name,
        // Etiqueta para auditoria
        @AuditFriendlyId
        String userName,
        String email,
        boolean activated,
        Set<String> permissions,
        boolean forcePasswordChange
) {}
