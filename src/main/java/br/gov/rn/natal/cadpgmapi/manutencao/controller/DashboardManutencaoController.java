package br.gov.rn.natal.cadpgmapi.manutencao.controller;

import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.DashboardManutencaoResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.service.DashboardManutencaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/manutencao/relatorios")
@RequiredArgsConstructor
@Tag(name = "Manutenção - Relatórios e Indicadores", description = "Métricas agregadas de tempo de resposta, resolução e volume")
public class DashboardManutencaoController {

    private final DashboardManutencaoService dashboardService;

    @GetMapping("/dashboard")
    @Operation(summary = "Obtém os dados consolidados do dashboard no período", description = "Retorna totais, médias de resposta/resolução em horas e séries agrupadas para gráficos.")
    public ResponseEntity<DashboardManutencaoResponseDTO> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return ResponseEntity.ok(dashboardService.gerarDashboard(dataInicio, dataFim));
    }
}
