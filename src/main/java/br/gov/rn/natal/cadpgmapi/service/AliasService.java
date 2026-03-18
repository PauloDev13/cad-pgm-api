package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.AliasRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.AliasResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Alias;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.AliasMapper;
import br.gov.rn.natal.cadpgmapi.repository.AliasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AliasService {
    private final AliasRepository aliasRepository;
    private final AliasMapper aliasMapper;

    @Transactional
    public AliasResponseDTO create(AliasRequestDTO dto) {
        if (aliasRepository.existsByEmail(dto.email())){
            throw new BusinessException("Alias já cadastrado!");
        }

        Alias entity = aliasMapper.toEntity(dto);
        return aliasMapper.toDto(aliasRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public AliasResponseDTO findById(Integer id) {
        Alias entity = aliasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alias não encontrado para o ID: " + id
                ));
        return aliasMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<AliasResponseDTO> findAll() {
        return aliasMapper.toDtoList(aliasRepository.findAll());
    }

    @Transactional
    public AliasResponseDTO update(Integer id, AliasRequestDTO dto) {
        Alias entity = aliasRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alias não encontrado para o ID: " + id
                ));

        aliasMapper.updateEntityFromDTO(entity, dto);
        return aliasMapper.toDto(aliasRepository.save(entity));
    }

    public void delete(Integer id) {
        if (aliasRepository.existsById(id)){
            throw new ResourceNotFoundException("Alias não encontrado");
        }
        aliasRepository.deleteById(id);
    }
}
