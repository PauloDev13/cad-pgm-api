package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.enums.TipoAtividade;

// Nó interno (O Servidor)
public record FolhaPontoServidorDTO(
        String nome,
        String vinculo,
        TipoAtividade tipoAtividade
) {}