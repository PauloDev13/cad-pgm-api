package br.gov.rn.natal.cadpgmapi.manutencao.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação simplificada de usuário apto a receber atribuição de manutenção")
public record ResponsavelComboResponseDTO(
        @Schema(description = "ID da conta de usuário", example = "15")
        Integer id,

        @Schema(description = "Nome do servidor ou usuário", example = "João da Silva")
        String nome,

        @Schema(description = "E-mail de contato", example = "joao.silva@natal.rn.gov.br")
        String email
) { }

