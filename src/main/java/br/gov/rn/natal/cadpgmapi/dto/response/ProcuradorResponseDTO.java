package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
import br.gov.rn.natal.cadpgmapi.enums.TipoCertificado;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Representação detalhada de um Procurador")
public record ProcuradorResponseDTO(
        @Schema(description = "Identificador único do procurador", example = "1")
        Integer id,

        // Etiqueta para auditoria
        @AuditFriendlyId
        @Schema(description = "Nome completo do procurador", example = "Dr. Carlos Alberto de Menezes")
        String nome,

        @Schema(description = "Tipo do certificado digital", example = "A1")
        TipoCertificado tipoCertificado,

        @Schema(description = "Data e hora de emissão do certificado", example = "2026-01-15T09:30:00")
        LocalDateTime dataExpedicao,

        @Schema(description = "Data e hora calculada de expiração do certificado", example = "2027-01-15T09:30:00")
        LocalDateTime dataExpiracao
) {}

