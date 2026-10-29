package br.gov.rn.natal.cadpgmapi.manutencao.repository;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.TipoServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoServicoRepository extends JpaRepository<TipoServico, Integer> {
    List<TipoServico> findByCategoriaIdAndAtivoTrueOrderByOrdemExibicaoAscNomeAsc(Integer categoriaId);
//    List<TipoServico> findByCategoriaIdOrderByOrdemExibicaoAscNomeAsc(Integer categoriaId);
    boolean existsByCategoriaIdAndNomeIgnoreCase(Integer categoriaId, String nome);
    boolean existsByCategoriaIdAndNomeIgnoreCaseAndIdNot(Integer categoriaId, String nome, Integer id);

    // TODOS OS TIPOS DE SERVIÇO POR CATEGORIA PAGINADA
    @EntityGraph(attributePaths = {"categoria"})
    Page<TipoServico> findByCategoriaId(Integer categoriaId, Pageable pageable);

}
