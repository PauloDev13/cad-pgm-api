package br.gov.rn.natal.cadpgmapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Schema(description = "Dados para criação ou atualização de um Vínculo")
public record VinculoRequestDTO(
        @Schema(description = "Descrição do vínculo", example = "Efetivo, Terceirizado")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
        String nome
) {
}
