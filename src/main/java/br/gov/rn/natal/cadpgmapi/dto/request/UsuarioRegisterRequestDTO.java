package br.gov.rn.natal.cadpgmapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRegisterRequestDTO(
        @NotBlank(message = "O Nome é obrigatório")
        @Size(max = 255, message = "O Nome deve ter no máximo 100 caracteres")
        String name,

        @NotBlank(message = "O Login é obrigatório")
        @Size(max = 30, message = "O Login deve ter no máximo 30 caracteres")
        String userName,

        @NotBlank(message = "O E-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        @Size(max = 255, message = "O email deve ter no máximo 100 caracteres")
        String email,

        @NotBlank(message = "A Senha é obrigatória")
        @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
        String password
) {
}
