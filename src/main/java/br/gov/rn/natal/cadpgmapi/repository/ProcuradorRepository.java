package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorSelectDTO;
import br.gov.rn.natal.cadpgmapi.entity.Procurador;
import br.gov.rn.natal.cadpgmapi.repository.generic.BaseNameRepository;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

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
}
