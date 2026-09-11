package br.gov.rn.natal.cadpgmapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Estrutura agrupada de procurador e seus servidores vinculados")
public record ProcuradorVinculoResponseDTO(
        @Schema(description = "Nome do Procurador", example = "Dr. Carlos Menezes")
        String nomeProcurador,

        @Schema(description = "Lista de servidores vinculados ao procurador")
        List<ServidorSetorDTO> servidores
) {
}
