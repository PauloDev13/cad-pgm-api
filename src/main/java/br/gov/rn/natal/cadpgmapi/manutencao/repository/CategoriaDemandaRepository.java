package br.gov.rn.natal.cadpgmapi.manutencao.repository;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.CategoriaDemanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaDemandaRepository extends JpaRepository<CategoriaDemanda, Integer> {
    List<CategoriaDemanda> findByAtivoTrueOrderByOrdemExibicaoAscNomeAsc();
    List<CategoriaDemanda> findAllByOrderByOrdemExibicaoAscNomeAsc();
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Integer id);
}
