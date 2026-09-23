package br.gov.rn.natal.cadpgmapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Schema(description = "Dados para criação ou atualização de um Usuário")
public record UsuarioRegisterRequestDTO(
        @Schema(description = "Nome completo do usuário", example = "Carlos Alberto de Menezes")
        @NotBlank(message = "O Nome é obrigatório")
        @Size(max = 255, message = "O Nome deve ter no máximo 100 caracteres")
        String name,

        @Schema(description = "Login do usuário", example = "carlos.alberto")
        @NotBlank(message = "O Login é obrigatório")
        @Size(max = 30, message = "O Login deve ter no máximo 30 caracteres")
        String userName,

        @Schema(description = "E-mail do usuário", example = "carlos.alberto@gmail.com")
        @NotBlank(message = "O E-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        @Size(max = 255, message = "O email deve ter no máximo 100 caracteres")
        String email,

        @Schema(description = "Senha do usuário", example = "12345")
        @NotBlank(message = "A Senha é obrigatória")
        @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
        String password
) {
}
