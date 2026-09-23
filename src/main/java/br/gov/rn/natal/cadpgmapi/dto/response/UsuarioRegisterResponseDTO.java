package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação de um Usuário")
public record UsuarioRegisterResponseDTO(
        @Schema(description = "Identificador único do usuário", example = "1")
        Integer id,
        @Schema(description = "Nome completo do usuário", example = "Carlos Alberto de Menezes")
        String name,
        @Schema(description = "Login do usuário", example = "carlos.alberto")
        // Etiqueta para auditoria
        @AuditFriendlyId
        String userName,
        @Schema(description = "E-mail do usuário", example = "carlos.alberto@gmail.com")
        String email
) {
}
