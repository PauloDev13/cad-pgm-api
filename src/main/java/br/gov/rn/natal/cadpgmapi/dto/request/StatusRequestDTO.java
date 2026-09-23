package br.gov.rn.natal.cadpgmapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
@Schema(description = "Dados para criação ou atualização de um Status")
public record StatusRequestDTO(
        @Schema(description = "Descrição do status", example = "Ativo, Pendente")
        @NotBlank(message = "A descrição é obrigatório")
        @Size(max = 50, message = "A descrição deve ter no máximo 50 caracteres")
        String descricao
) {
}
