package br.gov.rn.natal.cadpgmapi.audit.dtos;

import java.time.LocalDateTime;

public record AuditLogResponseDTO(
        Integer id,
        String username,
        LocalDateTime dateHourAction,
        String typeAction,
        String affectedEntity,
        String idAffectedRecord,
        String details
) {
}
