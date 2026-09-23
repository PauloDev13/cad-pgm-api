package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um Vínculo")
public record VinculoResponseDTO(
        @Schema(description = "Identificador único do vínculo", example = "1")
        Integer id,
        @Schema(description = "Nome do vínculo", example = "Efetivo, Terceirizado")
        // Etiqueta para auditoria
        @AuditFriendlyId
        String nome
) {}
