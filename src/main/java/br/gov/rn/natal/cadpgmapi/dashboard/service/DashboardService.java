package br.gov.rn.natal.cadpgmapi.dashboard.service;

import br.gov.rn.natal.cadpgmapi.dashboard.dto.response.DashboardSummaryDTO;
import br.gov.rn.natal.cadpgmapi.dashboard.dto.response.GraphItemDTO;
import br.gov.rn.natal.cadpgmapi.repository.ServidorRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardService {

    private final ServidorRepository servidorRepository;

    public DashboardService(ServidorRepository servidorRepository) {
        this.servidorRepository = servidorRepository;
    }

    @Transactional(readOnly = true)
    // Ativa o cache! Se 50 pessoas abrirem o dashboard no mesmo minuto,
    // o banco de dados só será consultado 1 vez. As outras 49 virão da memória RAM!
    @Cacheable(value = "dashboardResumoCache")
    public DashboardSummaryDTO obterResumoGeral() {

        // Dispara as 3 consultas otimizadas
        Long total = servidorRepository.countTotalServidoresAtivos();
        List<GraphItemDTO> porVinculo = servidorRepository.countDistribuicaoPorVinculo();
        List<GraphItemDTO> porStatus = servidorRepository.countDistribuicaoPorStatus();

        // Para deixar os status bonitos para o frontend (Opcional, caso seu Enum seja "LICENCA_MATERNIDADE")
        List<GraphItemDTO> porStatusFormatado = porStatus.stream()
                .map(item -> new GraphItemDTO(formatarRotulo(item.label()), item.quantity()))
                .toList();

        // Monta o payload final
        return new DashboardSummaryDTO(total, porVinculo, porStatusFormatado);
    }

    // Limpa o cache a cada 5 minutos (300.000 milissegundos)
    @CacheEvict(value = "dashboardResumoCache", allEntries = true)
    @Scheduled(fixedRateString = "300000")
    public void clearDashboardCache() {
        // O Spring executa esse método vazio a cada 5 min apenas para esvaziar a memória.
        // Na próxima requisição do front, o banco será consultado novamente.
    }

    // Método auxiliar para deixar o rótulo do status amigável (ex: LICENÇA_MATERNIDADE -> Licença Maternidade)
    private String formatarRotulo(String enumName) {
        if (enumName == null) return "Desconhecido";
        String formatado = enumName.replace("_", " ").toLowerCase();
        // Capitaliza a primeira letra
        return formatado.substring(0, 1).toUpperCase() + formatado.substring(1);
    }
}
