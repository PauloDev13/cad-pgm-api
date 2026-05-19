package br.gov.rn.natal.cadpgmapi.dashboard.dto.response;

import java.util.List;

// DTO principal que encapsula tudo (O JSON final)
public record DashboardSummaryDTO(
        Long totalServidores,
        List<GraphItemDTO> distributionByVinculo,
        List<GraphItemDTO> distributionByStatus
) {}
