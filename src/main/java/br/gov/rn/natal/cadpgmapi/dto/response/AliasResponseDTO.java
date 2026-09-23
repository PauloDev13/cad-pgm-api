package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um Alias (E-mail")
public record AliasResponseDTO(
        @Schema(description = "Identificador único do alias", example = "1")
        Integer id,
        @Schema(description = "Email vinculado ao alias", example = "ti.suporte@rn.gov.br")
        // Etiqueta para auditoria
        @AuditFriendlyId
        String email
) {}
