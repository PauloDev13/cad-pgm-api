package br.gov.rn.natal.cadpgmapi.manutencao.dto.response;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.AndarCodigo;

public record SalaResponseDTO(
        Integer id,
        String numeroIdentificacao,
        AndarCodigo andar,
        String rotuloAndar,
        String descricao,
        boolean ativo
) {}
