package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.entity.Setor;
import br.gov.rn.natal.cadpgmapi.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {
    boolean existsByDescricao(String descricao);
    Page<Status> findByDescricaoContainingIgnoreCase(String descricao, Pageable pageable);
}
