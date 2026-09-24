package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorSelectDTO;
import br.gov.rn.natal.cadpgmapi.entity.Procurador;
import br.gov.rn.natal.cadpgmapi.repository.generic.BaseNameRepository;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProcuradorRepository extends BaseNameRepository<Procurador, Integer> {


    @Query("""
     SELECT new br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorSelectDTO(
         p.id,
         p.nome
     )
     FROM Procurador p
         ORDER BY p.nome ASC
    """)
    // Otimiza busca no banco de dados para grandes quantidades de registros
    @QueryHints({
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
            @QueryHint(name = "org.jakarta.persistence.cache.retrieveMode", value = "USE"),
            @QueryHint(name = "org.jakarta.persistence.cache.storeMode", value = "USE"),
    })
    List<ProcuradorSelectDTO> findAllToCombo();

    /**
     * Busca os procuradores cujos certificados expiram dentro do intervalo especificado (início e fim do dia).
     */
    @Query("""
        SELECT new br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO(
            p.id,
            p.nome,
            p.tipoCertificado,
            p.dataExpedicao,
            p.dataExpiracao
        )
        FROM Procurador p
        WHERE p.dataExpiracao BETWEEN :inicioDia AND :fimDia
        ORDER BY p.nome ASC
    """)
    @QueryHints({
            @QueryHint(name = "org.hibernate.readOnly", value = "true"),
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
            @QueryHint(name = "org.jakarta.persistence.cache.retrieveMode", value = "USE"),
            @QueryHint(name = "org.jakarta.persistence.cache.storeMode", value = "USE"),
    })
    List<ProcuradorResponseDTO> findCertificadosExpirandoEntre(
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("fimDia") LocalDateTime fimDia
    );
}
