package br.gov.rn.natal.cadpgmapi.dto.request;

import br.gov.rn.natal.cadpgmapi.enums.TipoCertificado;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Dados para criação ou atualização de um Procurador")
public record ProcuradorRequestDTO(
        @Schema(description = "Nome completo do procurador", example = "Dr. Carlos Alberto de Menezes")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Schema(description = "Tipo do certificado digital ICP-Brasil", example = "A1")
        @NotNull(message = "O tipo do certificado é obrigatório")
        TipoCertificado tipoCertificado,

        @Schema(description = "Data e hora de expedição do certificado digital", example = "2026-01-15T09:30:00")
        @NotNull(message = "A data de expedição é obrigatória")
        @PastOrPresent(message = "A data de expedição não pode ser uma data futura")
        LocalDateTime dataExpedicao
) {
}

