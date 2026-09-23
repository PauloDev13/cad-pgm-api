package br.gov.rn.natal.cadpgmapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Schema(description = "Dados para criação ou atualização de um Alias (E-mail")
public record AliasRequestDTO(
        @Schema(description = "Endereço de E-mail", example = "carlos@gmail.com")
        @NotBlank(message = "O email é obrigatório")
        @Size(max = 100, message = "O email deve ter no máximo 100 caracteres")
        String email
) {
}
