package br.gov.rn.natal.cadpgmapi.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Classificação do tipo de feriado ou ponto facultativo")
public enum TipoFeriado {
    FERIADO_NACIONAL,
    FERIADO_ESTADUAL,
    FERIADO_MUNICIPAL,
    PONTO_FACULTATIVO
}
