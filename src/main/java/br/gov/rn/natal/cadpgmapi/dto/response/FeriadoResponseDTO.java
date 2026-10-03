package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.enums.TipoFeriado;
import io.swagger.v3.oas.annotations.media.Schema;
import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;

@Schema(description = "Representação de um Feriado / Ponto Facultativo")
public record FeriadoResponseDTO(
        @Schema(description = "Identificador único", example = "1")
        Integer id,

        @Schema(description = "Data no formato MM-dd", example = "04-21")
        String data,

        @Schema(description = "Descrição ou nome do feriado", example = "Natal")
        @AuditFriendlyId
        String nome,

        @Schema(description = "Tipo do feriado", example = "FERIADO_NACIONAL")
        TipoFeriado tipo,

        @Schema(description = "Status de inclusão na folha de ponto", example = "true")
        Boolean ativo
) {}
