package br.gov.rn.natal.cadpgmapi.manutencao.service;

import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.ChamadoManutencao;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.RequisicaoEcidade;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.RequisicaoEcidadeResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.repository.ChamadoManutencaoRepository;
import br.gov.rn.natal.cadpgmapi.manutencao.repository.RequisicaoEcidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequisicaoEcidadeService {
    private final RequisicaoEcidadeRepository requisicaoRepository;
    private final ChamadoManutencaoRepository chamadoRepository;

    @Transactional(readOnly = true)
    public List<RequisicaoEcidadeResponseDTO> listarPorChamado(Integer chamadoId) {
        return requisicaoRepository.findByChamadoIdOrderByCriadoEmAsc(chamadoId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public List<RequisicaoEcidadeResponseDTO> vincularRequisicoes(Integer chamadoId, List<String> numeros) {
        if (numeros == null || numeros.isEmpty()) {
            return List.of();
        }

        ChamadoManutencao chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado de manutenção não encontrado: " + chamadoId));

        Set<String> numerosSanitizados = numeros.stream()
                .filter(n -> n != null && !n.trim().isBlank())
                .map(String::trim)
                .collect(Collectors.toSet());

        List<RequisicaoEcidade> novasRequisicoes = new ArrayList<>();

        for (String num : numerosSanitizados) {
            if (!requisicaoRepository.existsByChamadoIdAndNumeroRequisicao(chamadoId, num)) {
                novasRequisicoes.add(RequisicaoEcidade.builder()
                        .chamado(chamado)
                        .numeroRequisicao(num)
                        .build());
            }
        }

        if (!novasRequisicoes.isEmpty()) {
            requisicaoRepository.saveAll(novasRequisicoes);
        }

        return listarPorChamado(chamadoId);
    }
    public RequisicaoEcidadeResponseDTO toResponseDTO(RequisicaoEcidade entity) {
        return new RequisicaoEcidadeResponseDTO(
                entity.getId(),
                entity.getNumeroRequisicao(),
                entity.getCriadoEm()
        );
    }
}
