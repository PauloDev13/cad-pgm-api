package br.gov.rn.natal.cadpgmapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação simplificada de Servidor para componentes de seleção")
public record ServidorComboDTO(
        Integer id,
        String nome
) { }
