package br.gov.rn.natal.cadpgmapi.manutencao.dto.request;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.PrioridadeChamado;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Dados para atualização administrativa/operacional do chamado de manutenção")
public record AtualizarDadosAdministrativosRequestDTO(
        @Schema(description = "Prioridade definida para o atendimento", example = "ALTA")
        PrioridadeChamado prioridade,

        @Schema(description = "Data limite acordada para a conclusão do serviço", example = "2026-11-20")
        LocalDate prazoConclusao,

        @Schema(description = "Número do tombo ou patrimônio do móvel relacionado", example = "15131104")
        @Size(max = 50, message = "O número do tombo/patrimônio deve ter no máximo 50 caracteres")
        String movelRelacionado,

        @Schema(description = "Lista com múltiplos números de requisições de materiais no e-Cidade",
                example = "[\"002359/2026\", \"002360/2026\"]")
        List<@Size(max = 50, message = "Cada requisição deve ter no máximo 50 caracteres") String> numerosRequisicaoEcidade
) {}
