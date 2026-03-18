package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.SetorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SetorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Setor;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.SetorMapper;
import br.gov.rn.natal.cadpgmapi.repository.SetorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SetorService {
    private final SetorRepository setorRepository;
    private final SetorMapper setorMapper;

    @Transactional
    public SetorResponseDTO create(SetorRequestDTO dto) {
        if (setorRepository.existsByNome(dto.nome())){
            throw new BusinessException("Setor já cadastrado!");
        }

        Setor entity = setorMapper.toEntity(dto);
        return setorMapper.toDto(setorRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public SetorResponseDTO findById(Integer id) {
        Setor entity = setorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Setor não encontrado para o ID: " + id
                ));
        return setorMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<SetorResponseDTO> findAll() {
        return setorMapper.toDtoList(setorRepository.findAll());
    }

    @Transactional
    public SetorResponseDTO update(Integer id, SetorRequestDTO dto) {
        Setor entity = setorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Setor não encontrado para o ID: " + id
                ));

        setorMapper.updateEntityFromDTO(entity, dto);
        return setorMapper.toDto(setorRepository.save(entity));
    }

    public void delete(Integer id) {
        if (setorRepository.existsById(id)){
            throw new ResourceNotFoundException("Servidor não encontrado");
        }
        setorRepository.deleteById(id);
    }
}
