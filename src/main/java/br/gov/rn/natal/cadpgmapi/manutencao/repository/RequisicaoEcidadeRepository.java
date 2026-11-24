package br.gov.rn.natal.cadpgmapi.manutencao.repository;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.RequisicaoEcidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequisicaoEcidadeRepository extends JpaRepository<RequisicaoEcidade, Integer> {
    List<RequisicaoEcidade> findByChamadoIdOrderByCriadoEmAsc(Integer chamadoId);
    boolean existsByChamadoIdAndNumeroRequisicao(Integer chamadoId, String numeroRequisicao);
    void deleteByChamadoId(Integer chamadoId);
}