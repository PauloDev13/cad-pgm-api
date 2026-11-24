package br.gov.rn.natal.cadpgmapi.manutencao.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Dados para encerramento do chamado")
public record ConcluirChamadoRequestDTO(
        @Schema(description = "Observação conclusiva dos reparos executados",
                example = "Reator e duas lâmpadas tubulares LED substituídos com sucesso.")
        @Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres")
        String observacao,

        @Schema(description = "Lista com múltiplos números de requisições de materiais no e-Cidade",
                example = "[\"002359/2026\", \"002360/2026\"]")
        List<@Size(max = 50, message = "Cada requisição deve ter no máximo 50 caracteres") String> numerosRequisicaoEcidade
) {}
