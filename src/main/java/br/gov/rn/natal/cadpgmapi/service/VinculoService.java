package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.VinculoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.VinculoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Vinculo;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.VinculoMapper;
import br.gov.rn.natal.cadpgmapi.repository.VinculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VinculoService {
    private final VinculoRepository vinculoRepository;
    private final VinculoMapper vinculoMapper;

    @Transactional
    public VinculoResponseDTO create(VinculoRequestDTO dto) {
        if (vinculoRepository.existsByNome(dto.nome())){
            throw new BusinessException("Vinculo já cadastrado!");
        }

        Vinculo entity = vinculoMapper.toEntity(dto);
        return vinculoMapper.toDto(vinculoRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public VinculoResponseDTO findById(Integer id) {
        Vinculo entity = vinculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vinculo não encontrado para o ID: " + id
                ));
        return vinculoMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<VinculoResponseDTO> findAll() {
        return vinculoMapper.toDtoList(vinculoRepository.findAll());
    }

    @Transactional
    public VinculoResponseDTO update(Integer id, VinculoRequestDTO dto) {
        Vinculo entity = vinculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vinculo não encontrado para o ID: " + id
                ));

        vinculoMapper.updateEntityFromDTO(entity, dto);
        return vinculoMapper.toDto(vinculoRepository.save(entity));
    }

    public void delete(Integer id) {
        if (vinculoRepository.existsById(id)){
            throw new ResourceNotFoundException("Vinculo não encontrado");
        }
        vinculoRepository.deleteById(id);
    }
}
