package br.gov.rn.natal.cadpgmapi.manutencao.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro e atualização de Subcategoria de Demanda")
public record SubcategoriaRequestDTO(
        @Schema(description = "ID da categoria pai vinculada", example = "2")
        @NotNull(message = "A categoria vinculada é obrigatória")
        Integer categoriaId,

        @Schema(description = "Nome da subcategoria", example = "Cadeiras e Longarinas")
        @NotBlank(message = "O nome da subcategoria é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @Schema(description = "Descrição detalhada da subcategoria",
                example = "Manutenção de cadeiras operacionais, giratórias e fixas"
        )
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        @Schema(description = "Ordem de exibição na listagem", example = "1")
        Integer ordemExibicao,

        @Schema(description = "Status de ativação", example = "true")
        Boolean ativo,

        @Schema(description = "Prazo estimado padrão em dias úteis para atendimento", example = "5")
        Integer prazoEstimadoDias,

        @Schema(description = "Indica se os chamados desta subcategoria exigem número de tombo/patrimônio",
                example = "true"
        )
        Boolean requerTombamento
) {}
