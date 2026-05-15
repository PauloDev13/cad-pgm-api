package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;

public record SetorResponseDTO(
        Integer id,
        // Etiqueta para auditoria
        @AuditFriendlyId
        String nome
) {}
