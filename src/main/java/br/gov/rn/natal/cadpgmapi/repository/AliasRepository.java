package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.entity.Alias;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AliasRepository extends JpaRepository<Alias, Integer> {
    Page<Alias> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    boolean existsByEmail(String email);
}
