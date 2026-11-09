package br.gov.rn.natal.cadpgmapi.manutencao.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados de retorno de uma Subcategoria de Demanda")
public record SubcategoriaResponseDTO(
        Integer id,
        Integer categoriaId,
        String categoriaNome,
        String nome,
        String descricao,
        boolean ativo,
        Integer ordemExibicao,
        Integer prazoEstimadoDias,
        boolean requerTombamento
) {}
