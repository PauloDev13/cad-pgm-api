package br.gov.rn.natal.cadpgmapi.manutencao.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para encerramento do chamado")
public record ConcluirChamadoRequestDTO(
        @Schema(description = "Observação conclusiva dos reparos executados", example = "Reator e duas lâmpadas tubulares LED substituídos com sucesso.")
        @Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres")
        String observacao,

        @Schema(description = "Número do protocolo da requisição de compra/material no sistema e-Cidade, caso materiais tenham sido utilizados",
                example = "002359/2026"
        )
        @Size(max = 50, message = "O número da requisição no e-Cidade deve ter no máximo 50 caracteres")
        String numeroRequisicaoEcidade
) {}
