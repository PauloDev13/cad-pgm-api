package br.gov.rn.natal.cadpgmapi.manutencao.repository;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.Sala;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.AndarCodigo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaRepository extends JpaRepository<Sala, Integer> {
    List<Sala> findByAndarAndAtivoTrueOrderByNumeroIdentificacaoAsc(AndarCodigo andar);
    List<Sala> findAllByOrderByAndarAscNumeroIdentificacaoAsc();
}

