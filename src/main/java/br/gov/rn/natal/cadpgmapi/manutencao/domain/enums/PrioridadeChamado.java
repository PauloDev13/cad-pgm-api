package br.gov.rn.natal.cadpgmapi.manutencao.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrioridadeChamado {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta"),
    URGENTE("Urgente");

    private final String rotulo;
}
