package br.gov.rn.natal.cadpgmapi.manutencao.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoExecutorChamado {
    EQUIPE_PROPRIA("Equipe própria"),
    EQUIPE_CONTRATADA("Equipe contratada"),
    OUTROS("Outros");

    private final String rotulo;
}
