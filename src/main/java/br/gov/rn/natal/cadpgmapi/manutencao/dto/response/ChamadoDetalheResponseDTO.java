package br.gov.rn.natal.cadpgmapi.manutencao.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.AndarCodigo;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.EstadoChamado;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.PrioridadeChamado;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.TipoExecutorChamado;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ChamadoDetalheResponseDTO(
        Integer id,
        @AuditFriendlyId
        String protocolo,
        AndarCodigo andar,
        String rotuloAndar,
        Integer salaId,
        String salaNome,
        String localDescricao,
        Integer categoriaId,
        String categoriaNome,
        Integer subcategoriaId,
        String subcategoriaNome,
        Integer tipoServicoId,
        String tipoServicoNome,
        String descricaoProblema,
        EstadoChamado estado,
        String rotuloEstado,
        PrioridadeChamado prioridade,
        String rotuloPrioridade,
        LocalDate prazoConclusao,
        String movelRelacionado,
        List<RequisicaoEcidadeResponseDTO> requisicoesEcidade,
        Integer solicitanteId,
        String solicitanteNome,
        Integer responsavelId,
        String responsavelNome,
        TipoExecutorChamado tipoExecutor,
        String rotuloTipoExecutor,
        String outroExecutor,
        LocalDateTime criadoEm,
        LocalDateTime atribuidoEm,
        LocalDateTime iniciadoEm,
        LocalDateTime concluidoEm,
        LocalDateTime canceladoEm,
        String motivoCancelamento,
        List<HistoricoChamadoResponseDTO> historico
) {}

