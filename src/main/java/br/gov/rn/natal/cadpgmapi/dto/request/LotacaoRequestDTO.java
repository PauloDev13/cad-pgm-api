package br.gov.rn.natal.cadpgmapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Schema(description = "Dados para criação ou atualização de uma Lotação")
public record LotacaoRequestDTO(
        @Schema(description = "Descrição da lotação", example = "PGM, SEMAD")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome
) {
}
