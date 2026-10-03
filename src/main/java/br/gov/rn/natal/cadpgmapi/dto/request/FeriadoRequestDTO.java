package br.gov.rn.natal.cadpgmapi.dto.request;

import br.gov.rn.natal.cadpgmapi.enums.TipoFeriado;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação ou atualização de um Feriado / Ponto Facultativo")
public record FeriadoRequestDTO(
        @Schema(description = "Data no formato MM-dd (mês e dia)", example = "04-21")
        @NotBlank(message = "A data é obrigatória")
        @Pattern(
                regexp = "^(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$",
                message = "A data deve estar no formato MM-dd (ex: 04-21 para 21 de abril)"
        )
        String data,

        @Schema(description = "Descrição ou nome do feriado", example = "Natal")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @Schema(description = "Tipo do feriado", example = "FERIADO_NACIONAL")
        @NotNull(message = "O tipo do feriado é obrigatório")
        TipoFeriado tipo,

        @Schema(description = "Flag de inclusão no relatório de ponto (padrão true)", example = "true")
        Boolean ativo
) {}
