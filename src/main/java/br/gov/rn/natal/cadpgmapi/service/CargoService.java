package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.CargoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.CargoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Cargo;
import br.gov.rn.natal.cadpgmapi.mapper.CargoMapper;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import br.gov.rn.natal.cadpgmapi.repository.CargoRepository;
import br.gov.rn.natal.cadpgmapi.repository.generic.BaseNameRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CargoService extends BaseNameGenericService<Cargo, CargoRequestDTO, CargoResponseDTO, Integer> {

    // Construtor
    public CargoService(CargoRepository repository, CargoMapper mapper) {
        super(repository, mapper);
    }
}
