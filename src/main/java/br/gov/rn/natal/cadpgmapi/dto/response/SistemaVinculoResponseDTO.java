package br.gov.rn.natal.cadpgmapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Estrutura agrupada de procurador e seus servidores vinculados")
public record SistemaVinculoResponseDTO(
        @Schema(description = "Nome do Sistema", example = "eCidade")
        String nomeSistema,

        @Schema(description = "Lista de servidores vinculados ao Sistema")
        List<String> servidores
) {
}
