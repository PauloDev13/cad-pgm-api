package br.gov.rn.natal.cadpgmapi.manutencao.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Representação de uma requisição do e-Cidade associada ao chamado")
public record RequisicaoEcidadeResponseDTO(
        Integer id,
        String numeroRequisicao,
        LocalDateTime criadoEm
) {}
