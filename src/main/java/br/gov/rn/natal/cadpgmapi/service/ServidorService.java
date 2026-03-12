package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.ServidorMapper;
import br.gov.rn.natal.cadpgmapi.repository.AliasRepository;
import br.gov.rn.natal.cadpgmapi.repository.ProcuradorRepository;
import br.gov.rn.natal.cadpgmapi.repository.ServidorRepository;
import br.gov.rn.natal.cadpgmapi.repository.SistemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServidorService {
    private final ServidorRepository servidorRepository;
    private final SistemaRepository sistemaRepository;
    private final AliasRepository aliasRepository;
    private final ProcuradorRepository procuradorRepository;
    private final ServidorMapper servidorMapper;

    @Transactional
    public ServidorResponseDTO create(ServidorRequestDTO dto) {
        if (servidorRepository.existsByCpf(dto.cpf())) {
            throw new BusinessException("Já existe um servidor cadastrado com este CPF.");
        }

        if (servidorRepository.existsByMatricula(dto.matricula())) {
            throw new BusinessException("Já existe um servidor cadastrado com esta Matrícula.");
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
    public ServidorResponseDTO findByCpfOrMatricula(String cpf, String matricula) {
        // Se enviou o CPF, prioriza a busca por CPF
        if (cpf != null && !cpf.isBlank()) {
            return servidorRepository.findByCpf(cpf)
                    .map(servidorMapper::toDTO)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Servidor não encontrado com o CPF: " + cpf
                    ));
        }

        // Se não enviou CPF, mas enviou Matrícula, busca por Matrícula
        if (matricula != null && !matricula.isBlank()) {
            return servidorRepository.findByMatricula(matricula)
                    .map(servidorMapper::toDTO)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Servidor não encontrado com a Matrícula: " + matricula
                    ));
        }

        // Se não enviou nenhum dos dois, lança um erro de regra de negócio
        throw new BusinessException("É obrigatório informar o CPF ou a Matrícula para realizar a busca.");
    }

    @Transactional(readOnly = true)
    public Page<ServidorResponseDTO> findAll(Pageable pageable) {
        return servidorRepository.findAll(pageable).map(servidorMapper::toDTO);
    }

    @Transactional
    public ServidorResponseDTO update(Integer id, ServidorRequestDTO dto) {
        // 1. Busca a entidade existente no banco (Entity em estado 'Managed' pelo Hibernate)
        Servidor entity = servidorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));

        // Em um cenário real, usar mapper.updateEntityFromDTO(dto, entity)
//        entity.setNome(dto.nome());

        // 2. Atualiza os dados básicos e relacionamentos N:1 mapeados usando MapStruct
        servidorMapper.updateEntityFromDTO(dto, entity);

        // 3. Reassocia as coleções N para N (Sistemas, Aliases, Procuradores) tratadas via Service
        associarRelacoesMuitosParaMuitos(entity, dto);

        return servidorMapper.toDTO(servidorRepository.save(entity));
    }

    @Transactional
    public void delete(Integer id) {
        if (!servidorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Servidor não encontrado");
        }
        servidorRepository.deleteById(id);
    }

    /**
     * Recebe a entidade (já mapeada com os dados básicos pelo MapStruct)
     * e os IDs vindos do DTO para fazer a associação otimizada.
     */
    private void associarRelacoesMuitosParaMuitos(Servidor entity, ServidorRequestDTO dto) {

        // Associa Sistemas
        if (dto.sistemaIds() != null) {
            entity.setSistemas(dto.sistemaIds().stream()
                    .map(sistemaRepository::getReferenceById) // Otimização: cria proxy sem SELECT
                    .collect(Collectors.toSet()));
        }

        // Associa Aliases de E-mail
        if (dto.aliasIds() != null) {
            entity.setAliases(dto.aliasIds().stream()
                    .map(aliasRepository::getReferenceById)
                    .collect(Collectors.toSet()));
        }

        // Associa Procuradores
        if (dto.procuradorIds() != null) {
            entity.setProcuradores(dto.procuradorIds().stream()
                    .map(procuradorRepository::getReferenceById)
                    .collect(Collectors.toSet()));
        }
    }
}
