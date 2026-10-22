package br.gov.rn.natal.cadpgmapi.manutencao.domain.enums;

import lombok.Getter;

@Getter
public enum EstadoChamado {
    ABERTO("Aberto"),
    ATRIBUIDO("Atribuído"),
    EM_ATENDIMENTO("Em Atendimento"),
    CONCLUIDO("Concluído"),
    CANCELADO("Cancelado");

    private final String rotulo;

    EstadoChamado(String rotulo) {
        this.rotulo = rotulo;
    }

    /**
     * Valida as transições de estado estritas definidas pelo negócio.
     */
    public boolean podeTransicionarPara(EstadoChamado novoEstado) {
        if (novoEstado == null) {
            return false;
        }

        return switch (this) {
            case ABERTO -> novoEstado == ATRIBUIDO || novoEstado == CANCELADO;
            case ATRIBUIDO -> novoEstado == EM_ATENDIMENTO || novoEstado == ATRIBUIDO || novoEstado == CANCELADO;
            case EM_ATENDIMENTO -> novoEstado == CONCLUIDO || novoEstado == CANCELADO;
            case CONCLUIDO, CANCELADO -> false; // Estados finais imutáveis
        };
    }

    public boolean isFinalizado() {
        return this == CONCLUIDO || this == CANCELADO;
    }
}
