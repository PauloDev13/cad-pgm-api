package br.gov.rn.natal.cadpgmapi.manutencao.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para início do atendimento do chamado")
public record IniciarAtendimentoRequestDTO(
        @Schema(description = "Observação opcional ao iniciar a execução", example = "Iniciado reparo técnico no local")
        @Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres")
        String observacao
) {}
