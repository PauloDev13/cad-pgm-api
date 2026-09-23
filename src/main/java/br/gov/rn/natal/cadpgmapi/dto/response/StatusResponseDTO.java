package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Representação de um Status")
public record StatusResponseDTO(
        @Schema(description = "Identificador único do status", example = "1")
        Integer id,
        @Schema(description = "Descrição do status", example = "Ativo, Pendente")
        // Etiqueta para auditoria
        @AuditFriendlyId
        String descricao
) {}
