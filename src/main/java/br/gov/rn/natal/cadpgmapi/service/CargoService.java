package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.CargoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.CargoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Cargo;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.CargoMapper;
import br.gov.rn.natal.cadpgmapi.repository.CargoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CargoService {
    private final CargoRepository cargoRepository;
    private final CargoMapper cargoMapper;

    @Transactional
    public CargoResponseDTO create(CargoRequestDTO dto) {
        if (cargoRepository.existsByNome(dto.nome())){
            throw new BusinessException("Cargo já cadastrado!");
        }

        Cargo entity = cargoMapper.toEntity(dto);
        return cargoMapper.toDto(cargoRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public CargoResponseDTO findById(Integer id) {
        Cargo entity = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cargo não encontrado para o ID: " + id
                ));
        return cargoMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<CargoResponseDTO> findAll() {
        return cargoMapper.toDtoList(cargoRepository.findAll());
    }

    @Transactional
    public CargoResponseDTO update(Integer id, CargoRequestDTO dto) {
        Cargo entity = cargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cargo não encontrado para o ID: " + id
                ));

        cargoMapper.updateEntityFromDTO(entity, dto);
        return cargoMapper.toDto(cargoRepository.save(entity));
    }

    public void delete(Integer id) {
        if (cargoRepository.existsById(id)){
            throw new ResourceNotFoundException("Cargo não encontrado");
        }
        cargoRepository.deleteById(id);
    }
}
