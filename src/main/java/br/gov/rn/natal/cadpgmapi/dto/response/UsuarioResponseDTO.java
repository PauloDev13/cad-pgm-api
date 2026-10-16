package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
@Schema(description = "Representação detalhada de um Usuário")
public record UsuarioResponseDTO(
        @Schema(description = "Identificador único do usuário", example = "1")
        Integer id,

        @Schema(description = "Nome completo (derivado do Servidor ou da conta pgmnet)", example = "Carlos Alberto de Menezes")
        String name,

        @Schema(description = "Login de autenticação", example = "carlos.alberto")
        // Etiqueta para auditoria
        @AuditFriendlyId
        String userName,

        @Schema(description = "E-mail oficial (derivado do Servidor ou da conta pgmnet)", example = "carlos.menezes@natal.rn.gov.br")
        String email,

        @Schema(description = "Status do usuário", example = "true")
        boolean activated,

        @Schema(description = "Permissões de acesso", example = "admin, guest")
        Set<String> permissions,

        @Schema(description = "Exige troca de senha no próximo login", example = "false")
        boolean forcePasswordChange,

        @Schema(description = "Matrícula do Servidor vinculado", example = "55025-8")
        String matriculaServidor,

        @Schema(description = "Cargo do Servidor vinculado", example = "Assessor Jurídico")
        String cargoServidor
) {}
