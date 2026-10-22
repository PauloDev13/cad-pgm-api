package br.gov.rn.natal.cadpgmapi.manutencao.repository;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.HistoricoChamado;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoChamadoRepository extends JpaRepository<HistoricoChamado, Integer> {

    @EntityGraph(attributePaths = {"ator", "ator.servidor", "responsavelAnterior", "responsavelNovo"})
    List<HistoricoChamado> findByChamadoIdOrderByDataHoraAsc(Integer chamadoId);
}
