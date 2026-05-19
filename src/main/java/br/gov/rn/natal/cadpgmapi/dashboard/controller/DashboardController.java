package br.gov.rn.natal.cadpgmapi.dashboard.controller;

import br.gov.rn.natal.cadpgmapi.dashboard.dto.response.DashboardSummaryDTO;
import br.gov.rn.natal.cadpgmapi.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboards")
@Tag(name = "Dashboards", description = "Endpoints para indicadores e gráficos gerenciais")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/servidores-resumo")
    @Operation(summary = "Resumo Geral dos Servidores",
            description = "Retorna os totais agrupados para renderização dos gráficos da página inicial")
    public ResponseEntity<DashboardSummaryDTO> getResumoGeral() {

        DashboardSummaryDTO resumo = dashboardService.obterResumoGeral();

        return ResponseEntity.ok(resumo);
    }
}
