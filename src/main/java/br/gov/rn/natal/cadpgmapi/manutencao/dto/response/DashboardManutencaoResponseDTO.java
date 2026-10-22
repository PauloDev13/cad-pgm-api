package br.gov.rn.natal.cadpgmapi.manutencao.dto.response;

import java.util.List;

public record DashboardManutencaoResponseDTO(
        long totalAbertos,
        long totalConcluidos,
        long totalCancelados,
        Double tempoMedioRespostaHoras,
        long qtdChamadosTempoResposta,
        Double tempoMedioResolucaoHoras,
        long qtdChamadosTempoResolucao,
        List<ItemAgrupadoDTO> volumePorTipoServico,
        List<ItemAgrupadoDTO> volumePorCategoria,
        List<ItemAgrupadoDTO> volumePorEstado
) {}
