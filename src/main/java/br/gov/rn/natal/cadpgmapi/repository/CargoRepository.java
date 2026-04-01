package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.entity.Cargo;
import br.gov.rn.natal.cadpgmapi.repository.generic.BaseNameRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CargoRepository extends BaseNameRepository<Cargo, Integer> {
}
