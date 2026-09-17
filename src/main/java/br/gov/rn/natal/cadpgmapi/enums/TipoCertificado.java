package br.gov.rn.natal.cadpgmapi.enums;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public enum TipoCertificado {
    A1("Certificado A1 (Validade 1 ano)", 1),
    A3("Certificado A3 (Validade 3 anos)", 3);

    private final String descricao;
    private final int anosValidade;

    TipoCertificado(String descricao, int anosValidade) {
        this.descricao = descricao;
        this.anosValidade = anosValidade;
    }

    /**
     * Calcula a data de expiração a partir da data de expedição com base na vigência do certificado.
     *
     * @param dataExpedicao Data/hora em que o certificado foi emitido.
     * @return Data/hora de expiração calculada ou null se dataExpedicao for nula.
     */
    public LocalDateTime calcularDataExpiracao(LocalDateTime dataExpedicao) {
        if (dataExpedicao == null) {
            return null;
        }
        return dataExpedicao.plusYears(this.anosValidade);
    }
}
