package br.gov.rn.natal.cadpgmapi.dto.update;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "DTO para atualização exclusiva do status do checkbox de folha de ponto")
public record FeriadoStatusPatchDTO(
        @Schema(description = "Novo status (true para considerar na folha, false para ignorar)", example = "true")
        @NotNull(message = "O campo ativo é obrigatório")
        Boolean ativo
) {}
