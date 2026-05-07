package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;

public record UsuarioRegisterResponseDTO(
        Integer id,
        String name,
        String userName,

        @AuditFriendlyId
        String email
) {
}
