package br.gov.rn.natal.cadpgmapi.manutencao.dto.response;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.AndarCodigo;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.EstadoChamado;

import java.time.LocalDateTime;

public record ChamadoResumoResponseDTO(
        Integer id,
        String protocolo,
        AndarCodigo andar,
        String rotuloAndar,
        String localExibicao,
        Integer salaId,
        String categoriaNome,
        String tipoServicoNome,
        EstadoChamado estado,
        String rotuloEstado,
        String solicitanteNome,
        String responsavelNome,
        LocalDateTime criadoEm,
        LocalDateTime atribuidoEm,
        LocalDateTime concluidoEm
) {}
