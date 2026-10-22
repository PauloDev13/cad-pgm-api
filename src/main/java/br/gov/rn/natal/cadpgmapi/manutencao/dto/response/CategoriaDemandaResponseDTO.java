package br.gov.rn.natal.cadpgmapi.manutencao.dto.response;

public record CategoriaDemandaResponseDTO(
        Integer id,
        String nome,
        String descricao,
        boolean ativo,
        Integer ordemExibicao
) {}
