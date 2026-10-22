package br.gov.rn.natal.cadpgmapi.manutencao.dto.response;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.AndarCodigo;

public record AndarResponseDTO(
        AndarCodigo codigo,
        String rotulo
) {}
