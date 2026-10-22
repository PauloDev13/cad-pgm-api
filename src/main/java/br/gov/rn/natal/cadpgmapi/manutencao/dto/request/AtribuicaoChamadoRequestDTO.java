package br.gov.rn.natal.cadpgmapi.manutencao.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atribuir ou reatribuir um responsável ao chamado")
public record AtribuicaoChamadoRequestDTO(
        @Schema(description = "ID do usuário do Almoxarifado responsável pelo atendimento", example = "5")
        @NotNull(message = "O responsável é obrigatório")
        Integer responsavelId,

        @Schema(description = "Observação sobre a distribuição ou motivo da reatribuição", example = "Encaminhado para a equipe de elétrica")
        @Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres")
        String observacao
) {}
