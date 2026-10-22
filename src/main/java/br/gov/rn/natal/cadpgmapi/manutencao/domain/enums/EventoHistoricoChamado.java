package br.gov.rn.natal.cadpgmapi.manutencao.domain.enums;

import lombok.Getter;

@Getter
public enum EventoHistoricoChamado {
    CRIACAO("Abertura do chamado"),
    ATRIBUICAO("Atribuição de responsável"),
    REATRIBUICAO("Reatribuição de responsável"),
    INICIO_ATENDIMENTO("Início do atendimento"),
    CONCLUSAO("Conclusão do atendimento"),
    CANCELAMENTO("Cancelamento da solicitação");

    private final String descricao;

    EventoHistoricoChamado(String descricao) {
        this.descricao = descricao;
    }
}
