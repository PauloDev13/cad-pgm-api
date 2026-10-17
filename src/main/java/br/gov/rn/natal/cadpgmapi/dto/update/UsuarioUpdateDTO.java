package br.gov.rn.natal.cadpgmapi.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UsuarioUpdateDTO(
        @Schema(description = "Login do usuário", example = "carlos.menezes")
        @NotBlank(message = "O Login é obrigatório")
        @Size(max = 30, message = "O Login deve ter no máximo 30 caracteres")
        String userName,

        @Schema(description = "Status de ativação", example = "true")
        boolean activated,

        @Schema(description = "Permissões do usuário", example = "[\"admin\"]")
        Set<String> permissions,

        @Schema(description = "Exige troca de senha", example = "false")
        boolean forcePasswordChange,

        @Schema(description = "Nome (editável apenas para conta de sistema 'pgmnet')")
        String systemName,

        @Schema(description = "E-mail (editável apenas para conta de sistema 'pgmnet')")
        String systemEmail
) {
}
