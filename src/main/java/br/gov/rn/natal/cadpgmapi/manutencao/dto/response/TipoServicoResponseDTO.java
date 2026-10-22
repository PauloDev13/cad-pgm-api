package br.gov.rn.natal.cadpgmapi.manutencao.dto.response;

public record TipoServicoResponseDTO(
        Integer id,
        Integer categoriaId,
        String categoriaNome,
        String nome,
        String descricao,
        boolean ativo,
        Integer ordemExibicao
) {}
