package br.gov.rn.natal.cadpgmapi.manutencao.repository;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.Subcategoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Integer> {
    List<Subcategoria> findByCategoriaIdAndAtivoTrueOrderByOrdemExibicaoAscNomeAsc(Integer categoriaId);
    @EntityGraph(attributePaths = {"categoria"})
    Page<Subcategoria> findByCategoriaId(Integer categoriaId, Pageable pageable);
    boolean existsByCategoriaIdAndNomeIgnoreCase(Integer categoriaId, String nome);
    boolean existsByCategoriaIdAndNomeIgnoreCaseAndIdNot(Integer categoriaId, String nome, Integer id);
}
