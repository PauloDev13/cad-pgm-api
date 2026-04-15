package br.gov.rn.natal.cadpgmapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ForceChangePasswordRequestDTO(
        @NotBlank(message = "A Senha é obrigatória") String newPassword
) {
}
