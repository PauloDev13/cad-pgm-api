package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.mapper.ServidorMapper;
import br.gov.rn.natal.cadpgmapi.repository.ServidorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServidorService {
    private final ServidorRepository servidorRepository;
    private final ServidorMapper servidorMapper;

    @Transactional
    public ServidorResponseDTO create(ServidorRequestDTO dto) {
        if (servidorRepository.existsByCpf(dto.cpf())) {
            throw new BusinessException("Já existe um servidor cadastrado com este CPF.");
        }
        Servidor entity = servidorMapper.toEntity(dto);
        return servidorMapper.toDTO(servidorRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ServidorResponseDTO findById(Integer id) {
        Servidor entity = servidorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));
        return servidorMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<ServidorResponseDTO> findAll(Pageable pageable) {
        return servidorRepository.findAll(pageable).map(servidorMapper::toDTO);
    }

    @Transactional
    public ServidorResponseDTO update(Integer id, ServidorRequestDTO dto) {
        Servidor entity = servidorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));

        // Em um cenário real, usar mapper.updateEntityFromDTO(dto, entity)
//        entity.setNome(dto.nome());
        // ... atualiza demais campos
        servidorMapper.updateEntityFromDTO(dto, entity);

        return servidorMapper.toDTO(servidorRepository.save(entity));
    }

    @Transactional
    public void delete(Integer id) {
        if (!servidorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Servidor não encontrado");
        }
        servidorRepository.deleteById(id);
    }
}
}
