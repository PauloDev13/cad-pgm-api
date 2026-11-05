package br.gov.rn.natal.cadpgmapi.manutencao.dto.response;

public record TipoServicoResponseDTO(
        Integer id,
        Integer categoriaId,
        String categoriaNome,
        Integer subcategoriaId,
        String subcategoriaNome,
        String nome,
        String descricao,
        boolean ativo,
        Integer ordemExibicao
) {}
