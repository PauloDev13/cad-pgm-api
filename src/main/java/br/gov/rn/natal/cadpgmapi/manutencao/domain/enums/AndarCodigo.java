package br.gov.rn.natal.cadpgmapi.manutencao.domain.enums;

import lombok.Getter;

@Getter
public enum AndarCodigo {
    TERREO("Térreo"),
    PRIMEIRO_ANDAR("Primeiro andar");

    private final String rotulo;

    AndarCodigo(String rotulo) {
        this.rotulo = rotulo;
    }
}
