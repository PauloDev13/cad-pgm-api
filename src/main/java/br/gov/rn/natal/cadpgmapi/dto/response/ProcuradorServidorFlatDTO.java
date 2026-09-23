package br.gov.rn.natal.cadpgmapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Registro plano para projeção de alto desempenho via JPQL Constructor Expression.
 */
@Schema(description = "Representação plana para projeção via JPQL")
public record ProcuradorServidorFlatDTO(
        @Schema(description = "Nome do Procurador", example = "Dr. Carlos Menezes")
        String nomeProcurador,
        @Schema(description = "Nome do Servidor", example = "Adriana Medeiros")
        String nomeServidor,
        @Schema(description = "Nome do Setor", example = "Departamento de TI")
        String nomeSetor
) {
}
