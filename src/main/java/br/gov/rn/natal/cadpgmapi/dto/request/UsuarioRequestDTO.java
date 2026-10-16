package br.gov.rn.natal.cadpgmapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;
@Schema(description = "Dados para criação ou atualização de um Usuário")
public record UsuarioRequestDTO(
        @Schema(description = "ID do servidor vinculado (obrigatório exceto para pgmnet)", example = "10")
        Integer servidorId,

        @Schema(description = "Login do usuário", example = "carlos.alberto")
        @NotBlank(message = "O Login é obrigatório")
        @Size(max = 30, message = "O Login deve ter no máximo 30 caracteres")
        String userName,

        @Schema(description = "Senha do usuário", example = "Senha@123")
        @NotBlank(message = "A Senha é obrigatória")
        @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
        String password,

        @Schema(description = "Status do usuário", example = "true")
        boolean activated,

        @Schema(description = "Perfis/permissões de acesso", example = "admin, guest")
        Set<String> permissions,

        @Schema(description = "Forçar troca de senha no próximo login", example = "false")
        boolean forcePasswordChange,

        @Schema(description = "Nome de exibição (apenas para conta pgmnet)")
                String systemName,

        @Schema(description = "E-mail de contato (apenas para conta pgmnet)")
        String systemEmail
) {
}
