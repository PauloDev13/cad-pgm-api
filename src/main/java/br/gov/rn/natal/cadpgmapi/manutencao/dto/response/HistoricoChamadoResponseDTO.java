package br.gov.rn.natal.cadpgmapi.manutencao.dto.response;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.EstadoChamado;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.EventoHistoricoChamado;

import java.time.LocalDateTime;

public record HistoricoChamadoResponseDTO(
        Integer id,
        EventoHistoricoChamado evento,
        String descricaoEvento,
        EstadoChamado estadoAnterior,
        EstadoChamado estadoNovo,
        String responsavelAnteriorNome,
        String responsavelNovoNome,
        String atorNome,
        LocalDateTime dataHora,
        String observacao
) {}
