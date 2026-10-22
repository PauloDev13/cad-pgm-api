package br.gov.rn.natal.cadpgmapi.manutencao.service;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.ChamadoManutencao;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.EstadoChamado;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.DashboardManutencaoResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.ItemAgrupadoDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.repository.ChamadoManutencaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardManutencaoService {

    private final ChamadoManutencaoRepository chamadoRepository;

    @Transactional(readOnly = true)
    public DashboardManutencaoResponseDTO gerarDashboard(LocalDate inicioFiltro, LocalDate fimFiltro) {
        LocalDateTime inicio = (inicioFiltro != null) ? inicioFiltro.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime fim = (fimFiltro != null) ? fimFiltro.atTime(23, 59, 59) : LocalDateTime.now();

        long totalAbertos = chamadoRepository.countTotalNoPeriodo(inicio, fim);
        long totalConcluidos = chamadoRepository.countPorEstadoNoPeriodo(inicio, fim, EstadoChamado.CONCLUIDO);
        long totalCancelados = chamadoRepository.countPorEstadoNoPeriodo(inicio, fim, EstadoChamado.CANCELADO);

        // Agrupamentos
        List<ItemAgrupadoDTO> volumeTipo = chamadoRepository.aggregateVolumePorTipo(inicio, fim);
        List<ItemAgrupadoDTO> volumeCategoria = chamadoRepository.aggregateVolumePorCategoria(inicio, fim);
        List<ItemAgrupadoDTO> volumeEstado = chamadoRepository.aggregateVolumePorEstado(inicio, fim);

        // Cálculo exato de tempo médio de resposta e resolução (em horas corridas)
        // Para garantir precisão, consulta os chamados do período com atribuição/conclusão válidas
        List<ChamadoManutencao> chamadosPeriodo = chamadoRepository.findAll((root, query, cb) ->
                cb.between(root.get("criadoEm"), inicio, fim)
        );

        // 1. Tempo de resposta: CriadoEm até AtribuidoEm (excluindo chamados cancelados antes da atribuição)
        List<Long> temposRespostaHoras = chamadosPeriodo.stream()
                .filter(c -> c.getAtribuidoEm() != null && (c.getCanceladoEm() == null || c.getCanceladoEm().isAfter(c.getAtribuidoEm())))
                .map(c -> Duration.between(c.getCriadoEm(), c.getAtribuidoEm()).toHours())
                .toList();

        Double mediaResposta = temposRespostaHoras.isEmpty() ? null :
                temposRespostaHoras.stream().mapToLong(Long::longValue).average().orElse(0.0);

        // 2. Tempo de resolução: CriadoEm até ConcluidoEm (apenas concluídos)
        List<Long> temposResolucaoHoras = chamadosPeriodo.stream()
                .filter(c -> c.getConcluidoEm() != null && c.getEstado() == EstadoChamado.CONCLUIDO)
                .map(c -> Duration.between(c.getCriadoEm(), c.getConcluidoEm()).toHours())
                .toList();

        Double mediaResolucao = temposResolucaoHoras.isEmpty() ? null :
                temposResolucaoHoras.stream().mapToLong(Long::longValue).average().orElse(0.0);

        return new DashboardManutencaoResponseDTO(
                totalAbertos,
                totalConcluidos,
                totalCancelados,
                mediaResposta,
                temposRespostaHoras.size(),
                mediaResolucao,
                temposResolucaoHoras.size(),
                volumeTipo,
                volumeCategoria,
                volumeEstado
        );
    }
}
