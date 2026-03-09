package br.gov.rn.natal.cadpgmapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AliasRequestDTO(
        @NotBlank(message = "O email é obrigatório")
        @Size(max = 100, message = "O email deve ter no máximo 100 caracteres")
        String email
) {
}
