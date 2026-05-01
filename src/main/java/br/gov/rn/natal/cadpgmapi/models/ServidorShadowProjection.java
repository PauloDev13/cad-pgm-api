package br.gov.rn.natal.cadpgmapi.models;

public interface ServidorShadowProjection {
    Integer getId();
    Boolean getExcluded();
    String getCpf();
    String getMatricula();
    String getEmailPessoal();
    String getEmailInstitucional();
}