package br.gov.rn.natal.cadpgmapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Registro plano para projeção de alto desempenho via JPQL Constructor Expression.
 */
@Schema(description = "Representação plana para projeção via JPQL")
public record SistemaServidorFlatDTO(
        @Schema(description = "Nome do Sistema", example = "eCidade")
        String nomeSistema,
        @Schema(description = "Nome do Servidor", example = "José Almeida")
        String nomeServidor
) {
}
