package br.gov.rn.natal.cadpgmapi.manutencao.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para encerramento do chamado")
public record ConcluirChamadoRequestDTO(
        @Schema(description = "Observação conclusiva dos reparos executados", example = "Reator e duas lâmpadas tubulares LED substituídos com sucesso.")
        @Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres")
        String observacao
) {}
