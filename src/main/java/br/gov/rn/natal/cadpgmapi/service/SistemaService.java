package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.SistemaRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SistemaResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Sistema;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.SistemaMapper;
import br.gov.rn.natal.cadpgmapi.repository.SistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SistemaService {
    private final SistemaRepository sistemaRepository;
    private final SistemaMapper sistemaMapper;

    @Transactional
    public SistemaResponseDTO create(SistemaRequestDTO dto) {
        if (sistemaRepository.existsByNome(dto.nome())){
            throw new BusinessException("Sistema já cadastrado!");
        }

        Sistema entity = sistemaMapper.toEntity(dto);
        return sistemaMapper.toDto(sistemaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public SistemaResponseDTO findById(Integer id) {
        Sistema entity = sistemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sistema não encontrado para o ID: " + id
                ));
        return sistemaMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<SistemaResponseDTO> findAll() {
        return sistemaMapper.toDtoList(sistemaRepository.findAll());
    }

    @Transactional
    public SistemaResponseDTO update(Integer id, SistemaRequestDTO dto) {
        Sistema entity = sistemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sistema não encontrado para o ID: " + id
                ));

        sistemaMapper.updateEntityFromDTO(entity, dto);
        return sistemaMapper.toDto(sistemaRepository.save(entity));
    }

    public void delete(Integer id) {
        Sistema entity = sistemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sistema não encontrado para o ID: " + id
                ));
        sistemaRepository.delete(entity);
    }
}
