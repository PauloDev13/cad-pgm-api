package br.gov.rn.natal.cadpgmapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados do servidor vinculado e seu respectivo setor")
public record ServidorSetorDTO(
        @Schema(description = "Nome completo do servidor", example = "Ana Beatriz Souza")
        String nomeServidor,
        @Schema(description = "Nome do setor do servidor", example = "Gabinete dos Procuradores")
        String nomeSetor
) {
}
