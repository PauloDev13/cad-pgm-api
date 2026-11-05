package br.gov.rn.natal.cadpgmapi.manutencao.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubcategoriaRequestDTO(
        @NotNull(message = "A categoria vinculada é obrigatória")
        Integer categoriaId,
        @NotBlank(message = "O nome da subcategoria é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,
        Integer ordemExibicao,
        Boolean ativo,
        Integer prazoEstimadoDias,
        Boolean requerTombamento
) {}
