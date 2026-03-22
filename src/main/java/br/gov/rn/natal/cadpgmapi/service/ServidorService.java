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
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

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

        // Converte o DTO para Entidade
        Servidor entity = servidorMapper.toEntity(dto);

        // 2. SALVA PRIMEIRO! (Isso gera o ID do Servidor no banco de dados)
        // Agora a entidade está "Managed" e tem um ID válido.
        entity = servidorRepository.save(entity);

        // 3. Associa as relações NN (Sistemas, Aliases, Procuradores)
        associarRelacoesMuitosParaMuitos(entity, dto);

        // O Hibernate, ao final do méthod @Transactional, vai perceber que
        // as listas mudaram e vai fazer os INSERTs nas tabelas de junção sozinho!
        return servidorMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public ServidorResponseDTO findById(Integer id) {
        Servidor entity = servidorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));
        return servidorMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<ServidorResponseDTO> findByFilters(
            String cpf, String matricula, String nome, Integer statusId, Pageable pageable
    ) {
        // BLOCO DA SPECIFICATION: Monta as regras (a "receita" do SQL)
        Specification<Servidor> spec = (root, query, cb) -> {
            // Começa neutro (1=1)
            Predicate predicate = cb.conjunction();

            // Se o CPF for imformado, monta o SQL de busca por CPF
            if (cpf != null && !cpf.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.like(
                        root.get("cpf"), "%" + cpf.trim() + "%")
                );
            }

            // Se a Matrícula for imformada, monta o SQL de busca por matrícula
            if (matricula != null && !matricula.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.like(
                        cb.lower(root.get("matricula")), "%" + matricula.trim().toLowerCase() + "%")
                );
            }

            if (nome!= null && !nome.trim().isEmpty()) {
                predicate = cb.and(
                        predicate, cb.like(
                        cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%")
                );

            }

            // Se o ID do Status for imformado, monta o SQL de busca pelo ID do Status
            if (statusId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status").get("id"), statusId));
            }

            // Encerra a montagem das regras
            return predicate;
        };

        // BLOCO DE EXECUÇÃO: Vai no banco e converte para DTO
        // O retorno real que vai para o Controller
        return servidorRepository.findAll(spec, pageable)
                .map(servidorMapper::toDTO);
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

        if (!entity.getMatricula().equals(dto.matricula())) {
            if (servidorRepository.existsByMatricula(dto.matricula())) {
                throw new BusinessException("Já existe um servidor cadastrado com esta Matrícula.");
            }
        }

        // Atualiza os dados básicos e relacionamentos N:1 mapeados usando MapStruct
        servidorMapper.updateEntityFromDTO(dto, entity);

        // Reassocia as coleções N para N (Sistemas, Aliases, Procuradores) tratadas via Service
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
        if (dto.sistemaIds() != null && !dto.sistemaIds().isEmpty()) {

            // Cria uma lista caso ela seja nula ou limpa a lista se ela existir
            if (entity.getSistemas() == null) {
                entity.setSistemas(new HashSet<>());
            } else {
                entity.getSistemas().clear();
            }

            // Adiciona os IDs à tabela de junção
            dto.sistemaIds().forEach(id -> {
                entity.getSistemas().add(sistemaRepository.getReferenceById(id));
            });
        }

        // Associa Aliases de E-mail
        if (dto.aliasIds() != null && !dto.aliasIds().isEmpty()) {
            if (entity.getAliases() == null) {
                entity.setAliases(new HashSet<>());
            } else {
                entity.getAliases().clear();
            }

            dto.aliasIds().forEach(id -> {
                entity.getAliases().add(aliasRepository.getReferenceById(id));
            });
        }

        // Associa Procuradores
        if (dto.procuradorIds() != null && !dto.procuradorIds().isEmpty()) {
            if (entity.getProcuradores() == null) {
                entity.setProcuradores(new HashSet<>());
            } else {
                entity.getProcuradores().clear();
            }

            dto.procuradorIds().forEach(id -> {
                entity.getProcuradores().add(procuradorRepository.getReferenceById(id));
            });
        }
    }
}
