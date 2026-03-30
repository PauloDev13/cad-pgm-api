package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.CargoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.CargoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Cargo;
import br.gov.rn.natal.cadpgmapi.mapper.CargoMapper;
import br.gov.rn.natal.cadpgmapi.repository.CargoRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CargoService extends BaseGenericService<Cargo, CargoRequestDTO, CargoResponseDTO, Integer> {
    private final CargoRepository cargoRepository;

    public CargoService(CargoMapper mapper, CargoRepository repository) {
        super(repository, mapper);
        this.cargoRepository = repository;
    }

    @Transactional(readOnly = true)
    public Page<CargoResponseDTO> findByFilterName(String filter, Pageable pageable) {
        // Se o nome vier nulo ou vazio, você pode optar por retornar todos os registros
        if (filter == null || filter.trim().isEmpty()) {
            return repository.findAll(pageable)
                    .map(mapper::toDto);
        }

        // Executa a busca filtrada e paginada
        return cargoRepository.findByNomeContainingIgnoreCase(filter.trim(), pageable)
                .map(super.mapper::toDto);
    }
}
