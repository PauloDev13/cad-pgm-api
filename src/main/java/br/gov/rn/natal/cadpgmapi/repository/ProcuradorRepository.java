package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.entity.Procurador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcuradorRepository extends JpaRepository<Procurador, Integer> {
    boolean existsByNome(String nome);
}
