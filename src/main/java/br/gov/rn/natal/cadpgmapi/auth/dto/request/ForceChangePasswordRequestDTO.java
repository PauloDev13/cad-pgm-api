package br.gov.rn.natal.cadpgmapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ForceChangePasswordRequestDTO(
        // TODO: Provisório enquanto não temos o SecurityContext
        @NotBlank(message = "O Login é obrigatório") String userName,
        @NotBlank(message = "A Senha é obrigatória") String newPassword
) {
}
