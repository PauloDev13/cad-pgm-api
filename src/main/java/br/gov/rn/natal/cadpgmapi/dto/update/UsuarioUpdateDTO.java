package br.gov.rn.natal.cadpgmapi.dto.update;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UsuarioUpdateDTO(
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
        boolean activated,
        Set<String> permissions,
        boolean forcePasswordChange
) {
}
