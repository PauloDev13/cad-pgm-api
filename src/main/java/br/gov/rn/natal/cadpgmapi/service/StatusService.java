package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.StatusRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.StatusResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Status;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.StatusMapper;
import br.gov.rn.natal.cadpgmapi.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatusService {
    private final StatusRepository statusRepository;
    private final StatusMapper statusMapper;

    @Transactional
    public StatusResponseDTO create(StatusRequestDTO dto) {
        if (statusRepository.existsByDescricao(dto.descricao())){
            throw new BusinessException("Status já cadastrado!");
        }

        Status entity = statusMapper.toEntity(dto);
        return statusMapper.toDto(statusRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public StatusResponseDTO findById(Integer id) {
        Status entity = statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Status não encontrado para o ID: " + id
                ));
        return statusMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<StatusResponseDTO> findAll() {
        return statusMapper.toDtoList(statusRepository.findAll());
    }

    @Transactional
    public StatusResponseDTO update(Integer id, StatusRequestDTO dto) {
        Status entity = statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Status não encontrado para o ID: " + id
                ));

        statusMapper.updateEntityFromDTO(entity, dto);
        return statusMapper.toDto(statusRepository.save(entity));
    }

    public void delete(Integer id) {
        if (statusRepository.existsById(id)){
            throw new ResourceNotFoundException("Status não encontrado");
        }
        statusRepository.deleteById(id);
    }
}
