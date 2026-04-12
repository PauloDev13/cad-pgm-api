package br.gov.rn.natal.cadpgmapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank(message = "O Login é obrigatório")
        String login,

        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}
