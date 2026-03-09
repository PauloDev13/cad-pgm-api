package br.gov.rn.natal.cadpgmapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StatusRequestDTO(
        @NotBlank(message = "A descrição é obrigatório")
        @Size(max = 50, message = "A descrição deve ter no máximo 50 caracteres")
        String descricao
) {
}
