package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.enums.TipoAtividade;
import io.swagger.v3.oas.annotations.media.Schema;

// DTO para receber o retorno do SQL
@Schema(description = "Representação do retorno do SQL para o relatório da folha de ponto")
public record FolhaPontoProjectionDTO(
        @Schema(description = "Identificador único do setor", example = "1")
        Integer idSetor,
        @Schema(description = "Nome do setor", example = "Departamento de TI")
        String nomeSetor,
        @Schema(description = "Nome do servidor", example = "Carlos Alberto de Menezes")
        String nomeServidor,
        @Schema(description = "Descrição do vínculo", example = "Efetivo, Comissionado")
        String vinculo,

        @Schema(description = "Tipo de Atividade", example = "Presencial, Remoto")
        TipoAtividade tipoAtividade
) {}