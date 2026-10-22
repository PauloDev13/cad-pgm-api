package br.gov.rn.natal.cadpgmapi.manutencao.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cancelamento do chamado pelo solicitante")
public record CancelarChamadoRequestDTO(
        @Schema(description = "Motivo justificado do cancelamento", example = "Situação resolvida internamente pelo próprio setor")
        @NotBlank(message = "O motivo do cancelamento é obrigatório")
        @Size(max = 500, message = "O motivo pode ter no máximo 500 caracteres")
        String motivo
) {}
