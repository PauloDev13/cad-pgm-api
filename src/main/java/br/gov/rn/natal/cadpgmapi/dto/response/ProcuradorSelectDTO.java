package br.gov.rn.natal.cadpgmapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação resumida de um Procurador")
public record ProcuradorSelectDTO(
        @Schema(description = "Identificador único do procurador", example = "1")
        Integer id,

        @Schema(description = "Nome completo do procurador", example = "Dr. Carlos Alberto de Menezes")
        String nome
) {}

