package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.enums.TipoAtividade;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação do servidor no relatório de ponto")
public record FolhaPontoServidorDTO(
        @Schema(description = "Nome do servidor", example = "Carlos Alberto de Menezes")
        String nome,
        @Schema(description = "Descrição do vínculo", example = "Efetivo, Comissionado")
        String vinculo,
        @Schema(description = "Tipo de Atividade", example = "Presencial, Remoto")
        TipoAtividade tipoAtividade
) {}