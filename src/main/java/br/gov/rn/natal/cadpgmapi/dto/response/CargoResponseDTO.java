package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um Cargo")
public record CargoResponseDTO(
        @Schema(description = "Identificador único do cargo", example = "1")
        Integer id,
        @Schema(description = "Nome do cargo", example = "Diretor do Departamento de TI")
        // Etiqueta para auditoria
        @AuditFriendlyId
        String nome
) {}
