package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.enums.TipoAtividade;

public record FolhaPontoResponseDTO(
        String nome,
        String vinculo,
        String setor,
        TipoAtividade tipoAtividade
) {
}
