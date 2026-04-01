package br.gov.rn.natal.cadpgmapi.repository;

import br.gov.rn.natal.cadpgmapi.entity.Vinculo;
import br.gov.rn.natal.cadpgmapi.repository.generic.BaseNameRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VinculoRepository extends BaseNameRepository<Vinculo, Integer> {
}
