package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.CargoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.CargoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Cargo;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.CargoMapper;
import br.gov.rn.natal.cadpgmapi.repository.CargoRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CargoService extends BaseNameGenericService<Cargo, CargoRequestDTO, CargoResponseDTO, Integer> {
    private final CargoRepository cargoRepository;

    // Construtor
    public CargoService(
            CargoRepository repository,
            CargoMapper mapper,
            ApplicationEventPublisher eventPublisher) {
        super(repository, mapper, eventPublisher);
        this.cargoRepository = repository;
    }

    // ================================================================
    // MÉTODOS SOBRESCRITOS EXCLUSIVAMENTE PARA GERENCIAMENTO DO CACHE
    // ================================================================

    // CACHE DA LISTA DE DROPDOWNS (Sobrescrevendo o método avô)
    @Override
    @Cacheable(value = "cargosCache")
    public List<CargoResponseDTO> findAllSelect() {
        return super.findAllSelect();
    }

    // ================================================================
    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    // ================================================================

    @Override
    protected void beforeCreate(CargoRequestDTO dto) {
        if (cargoRepository.existsByNome(dto.nome().trim())) {
            throw new BusinessException("Já existe um <strong>Cargo</strong> cadastrado como " +
                    "(<strong>" + dto.nome() + "</strong>).");
        }
    }

    @Override
    protected void beforeUpdate(CargoRequestDTO dto, Cargo existingCargo) {
        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR o e-mail
        if (!existingCargo.getNome().equalsIgnoreCase(dto.nome())) {
            if (cargoRepository.existsByNome(dto.nome())) {
                throw new BusinessException("Este <strong>Cargo</strong> (<strong>" + dto.nome() +
                        "<strong>) já foi cadastrado.");
            }
        }
    }
}
