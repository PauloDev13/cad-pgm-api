package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;

public record UsuarioRegisterResponseDTO(
        Integer id,
        String name,
        // Etiqueta para auditoria
        @AuditFriendlyId
        String userName,
        String email
) {
}
