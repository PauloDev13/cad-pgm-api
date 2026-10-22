package br.gov.rn.natal.cadpgmapi.manutencao.dto.request;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.AndarCodigo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para abertura de um novo chamado de manutenção")
public record AberturaChamadoRequestDTO(
        @Schema(description = "Andar do prédio", example = "TERREO")
        @NotNull(message = "O andar é obrigatório")
        AndarCodigo andar,

        @Schema(description = "ID da sala cadastrada (nulo caso 'Outro')", example = "1")
        Integer salaId,

        @Schema(description = "Descrição textual do local (obrigatório se sala for 'Outro')", example = "Corredor lateral próximo à copa")
        @Size(max = 255, message = "A descrição do local deve ter no máximo 255 caracteres")
        String localDescricao,

        @Schema(description = "ID da categoria da demanda", example = "4")
        @NotNull(message = "A categoria do chamado é obrigatória")
        Integer categoriaId,

        @Schema(description = "ID do tipo de serviço específico", example = "15")
        @NotNull(message = "O tipo de serviço é obrigatório")
        Integer tipoServicoId,

        @Schema(description = "Detalhamento livre da demanda encontrada", example = "Lâmpada tubular queimada piscando insistentemente.")
        @NotBlank(message = "A descrição do chamado é obrigatória")
        @Size(max = 2000, message = "A descrição pode ter no máximo 2000 caracteres")
        String descricao
) {}
