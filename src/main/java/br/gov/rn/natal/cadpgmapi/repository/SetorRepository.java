package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.entity.Setor;
import br.gov.rn.natal.cadpgmapi.repository.generic.BaseNameRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SetorRepository extends BaseNameRepository<Setor, Integer> {
}
