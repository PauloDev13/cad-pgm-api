package br.gov.rn.natal.cadpgmapi.manutencao.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Payload para adição de múltiplos números de requisição e-Cidade ao chamado")
public record RequisicoesEcidadeRequestDTO(
        @Schema(description = "Lista de números/protocolos das requisições no e-Cidade",
                example = "[\"002359/2026\", \"002360/2026\"]")
        @NotEmpty(message = "Informe ao menos um número de requisição")
        List<@Size(max = 50, message = "O número da requisição deve ter no máximo 50 caracteres")
                String> numerosRequisicao
) { }
