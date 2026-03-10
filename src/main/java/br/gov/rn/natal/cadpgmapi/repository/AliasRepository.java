package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.entity.Alias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AliasRepository extends JpaRepository<Alias, Integer> {
    boolean existsByEmail(String email);
}
