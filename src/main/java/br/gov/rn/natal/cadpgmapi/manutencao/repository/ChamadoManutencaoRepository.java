package br.gov.rn.natal.cadpgmapi.manutencao.repository;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.ChamadoManutencao;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.EstadoChamado;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.ItemAgrupadoDTO;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChamadoManutencaoRepository extends JpaRepository<ChamadoManutencao, Integer>, JpaSpecificationExecutor<ChamadoManutencao> {

    @EntityGraph(attributePaths = {"solicitante", "solicitante.servidor", "responsavel", "responsavel.servidor", "sala", "categoria", "tipoServico"})
    Optional<ChamadoManutencao> findDetailById(Integer id);

    @EntityGraph(attributePaths = {"solicitante", "solicitante.servidor", "responsavel", "responsavel.servidor", "sala", "categoria", "tipoServico"})
    Page<ChamadoManutencao> findBySolicitanteId(Integer solicitanteId, Pageable pageable);

    @NotNull
    @Override
    @EntityGraph(attributePaths = {"solicitante", "solicitante.servidor", "responsavel", "responsavel.servidor", "sala", "categoria", "tipoServico"})
    Page<ChamadoManutencao> findAll(@NotNull Specification<ChamadoManutencao> spec, @NotNull Pageable pageable);

    boolean existsByTipoServicoId(Integer tipoServicoId);

    @Query("SELECT COUNT(c) FROM ChamadoManutencao c WHERE c.criadoEm >= :inicioDoDia")
    long countChamadosCriadosApos(@Param("inicioDoDia") LocalDateTime inicioDoDia);

    // --- AGREGADOS DO DASHBOARD ---

    @Query("""
        SELECT COUNT(c) FROM ChamadoManutencao c
        WHERE c.criadoEm BETWEEN :inicio AND :fim
    """)
    long countTotalNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
        SELECT COUNT(c) FROM ChamadoManutencao c
        WHERE c.criadoEm BETWEEN :inicio AND :fim
          AND c.estado = :estado
    """)
    long countPorEstadoNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, @Param("estado") EstadoChamado estado);

    @Query("""
        SELECT new br.gov.rn.natal.cadpgmapi.manutencao.dto.response.ItemAgrupadoDTO(c.tipoServico.nome, COUNT(c))
        FROM ChamadoManutencao c
        WHERE c.criadoEm BETWEEN :inicio AND :fim
        GROUP BY c.tipoServico.nome
        ORDER BY COUNT(c) DESC
    """)
    List<ItemAgrupadoDTO> aggregateVolumePorTipo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
        SELECT new br.gov.rn.natal.cadpgmapi.manutencao.dto.response.ItemAgrupadoDTO(c.categoria.nome, COUNT(c))
        FROM ChamadoManutencao c
        WHERE c.criadoEm BETWEEN :inicio AND :fim
        GROUP BY c.categoria.nome
        ORDER BY COUNT(c) DESC
    """)
    List<ItemAgrupadoDTO> aggregateVolumePorCategoria(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
        SELECT new br.gov.rn.natal.cadpgmapi.manutencao.dto.response.ItemAgrupadoDTO(STR(c.estado), COUNT(c))
        FROM ChamadoManutencao c
        WHERE c.criadoEm BETWEEN :inicio AND :fim
        GROUP BY c.estado
    """)
    List<ItemAgrupadoDTO> aggregateVolumePorEstado(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
