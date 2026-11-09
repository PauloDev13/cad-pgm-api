package br.gov.rn.natal.cadpgmapi.manutencao.repository;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.TipoServico;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoServicoRepository extends JpaRepository<TipoServico, Integer> {

    List<TipoServico> findBySubcategoriaIdAndAtivoTrueOrderByOrdemExibicaoAscNomeAsc(Integer subcategoriaId);

    List<TipoServico> findBySubcategoriaIdAndAtivoTrue(Integer subcategoriaId);

    List<TipoServico> findBySubcategoriaIdAndInativadoPorCascataTrue(Integer subcategoriaId);

    @EntityGraph(attributePaths = {"subcategoria", "subcategoria.categoria"})
    Page<TipoServico> findBySubcategoriaIdAndAtivo(Integer subcategoriaId, Boolean ativo, Pageable pageable);

    @EntityGraph(attributePaths = {"subcategoria", "subcategoria.categoria"})
    Page<TipoServico> findBySubcategoriaId(Integer subcategoriaId, Pageable pageable);

    @EntityGraph(attributePaths = {"subcategoria", "subcategoria.categoria"})
    Page<TipoServico> findByAtivo(Boolean ativo, Pageable pageable);

    @NotNull
    @Override
    @EntityGraph(attributePaths = {"subcategoria", "subcategoria.categoria"})
    Page<TipoServico> findAll(@NotNull Pageable pageable);

    boolean existsBySubcategoriaIdAndNomeIgnoreCase(Integer subcategoriaId, String nome);
    boolean existsBySubcategoriaIdAndNomeIgnoreCaseAndIdNot(Integer subcategoriaId, String nome, Integer id);

}
