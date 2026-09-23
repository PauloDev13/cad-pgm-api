package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um Setor")
public record SetorResponseDTO(
        @Schema(description = "Identificador único do setor", example = "1")
        Integer id,
        @Schema(description = "Nome do setor", example = "Departamento de TI")
        // Etiqueta para auditoria
        @AuditFriendlyId
        String nome
) {}
