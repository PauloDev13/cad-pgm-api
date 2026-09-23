package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
@Schema(description = "Representação de um Usuário")
public record UsuarioResponseDTO(
        @Schema(description = "Identificador único do usuário", example = "1")
        Integer id,
        @Schema(description = "Nome completo do usuário", example = "Carlos Alberto de Menezes")
        String name,
        @Schema(description = "Login do usuário", example = "carlos.alberto")
        // Etiqueta para auditoria
        @AuditFriendlyId
        String userName,
        @Schema(description = "E-mail do usuário", example = "carlos.alberto@gmail.com")
        String email,
        @Schema(description = "Status do usuário", example = "Ativo, Bloqueado")
        boolean activated,
        @Schema(description = "Profiles do usuário", example = "admin, guest")
        Set<String> permissions,
        @Schema(description = "Se o usuário é forçado a trocar a senha", example = "false")
        boolean forcePasswordChange
) {}
