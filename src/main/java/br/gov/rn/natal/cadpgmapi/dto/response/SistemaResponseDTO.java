package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um Sistema")
public record SistemaResponseDTO(
        @Schema(description = "Identificador único do sistema", example = "1")
        Integer id,
        @Schema(description = "Nome do sistema", example = "eCidade, eDoc")
        // Etiqueta para auditoria
        @AuditFriendlyId
        String nome
) {}
