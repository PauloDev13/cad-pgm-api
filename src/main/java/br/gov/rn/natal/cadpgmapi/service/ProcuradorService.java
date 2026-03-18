package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.ProcuradorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Procurador;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.ProcuradorMapper;
import br.gov.rn.natal.cadpgmapi.repository.ProcuradorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcuradorService {
    private final ProcuradorRepository procuradorRepository;
    private final ProcuradorMapper procuradorMapper;

    @Transactional
    public ProcuradorResponseDTO create(ProcuradorRequestDTO dto) {
        if (procuradorRepository.existsByNome(dto.nome())){
            throw new BusinessException("Procurador já cadastrado!");
        }

        Procurador entity = procuradorMapper.toEntity(dto);
        return procuradorMapper.toDto(procuradorRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ProcuradorResponseDTO findById(Integer id) {
        Procurador entity = procuradorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Procurador não encontrado para o ID: " + id
                ));
        return procuradorMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<ProcuradorResponseDTO> findAll() {
        return procuradorMapper.toDtoList(procuradorRepository.findAll());
    }

    @Transactional
    public ProcuradorResponseDTO update(Integer id, ProcuradorRequestDTO dto) {
        Procurador entity = procuradorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Procurador não encontrado para o ID: " + id
                ));

        procuradorMapper.updateEntityFromDTO(entity, dto);
        return procuradorMapper.toDto(procuradorRepository.save(entity));
    }

    public void delete(Integer id) {
        if (procuradorRepository.existsById(id)){
            throw new ResourceNotFoundException("Procurador não encontrado");
        }
        procuradorRepository.deleteById(id);
    }
}
