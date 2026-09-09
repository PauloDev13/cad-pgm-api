package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.enums.TipoAtividade;

// DTO para receber o retorno do SQL
public record FolhaPontoProjectionDTO(
        Integer idSetor,
        String nomeSetor,
        String nomeServidor,
        String vinculo,
        TipoAtividade tipoAtividade
) {}