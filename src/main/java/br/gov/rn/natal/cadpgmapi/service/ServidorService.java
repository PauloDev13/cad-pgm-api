package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.CargoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Cargo;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.ServidorMapper;
import br.gov.rn.natal.cadpgmapi.repository.AliasRepository;
import br.gov.rn.natal.cadpgmapi.repository.ProcuradorRepository;
import br.gov.rn.natal.cadpgmapi.repository.ServidorRepository;
import br.gov.rn.natal.cadpgmapi.repository.SistemaRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
public class ServidorService extends BaseGenericService<
        Servidor, ServidorRequestDTO, ServidorResponseDTO, Integer> {
    private final ServidorRepository servidorRepository;
    private final SistemaRepository sistemaRepository;
    private final AliasRepository aliasRepository;
    private final ProcuradorRepository procuradorRepository;

    // Construtor
    public ServidorService(
            ServidorRepository repository,
            ServidorMapper mapper,
            SistemaRepository sistemaRepository,
            AliasRepository aliasRepository,
            ProcuradorRepository procuradorRepository
    ){
        super(repository, mapper);
        this.servidorRepository = repository;
        this.sistemaRepository = sistemaRepository;
        this.aliasRepository = aliasRepository;
        this.procuradorRepository = procuradorRepository;
    }

    @Override
    @Transactional
    public ServidorResponseDTO create(ServidorRequestDTO dto) {
        // Chama a validação antes de criar um servidor
        beforeCreate(dto);

        // Converte o DTO para Entidade
        Servidor entity = mapper.toEntity(dto);

        // 2. SALVA PRIMEIRO! (Isso gera o ID do Servidor no banco de dados)
        // Agora a entidade está "Managed" e tem um ID válido.
        entity = servidorRepository.save(entity);

        // 3. Associa as relações NN (Sistemas, Aliases, Procuradores)
        associarRelacoesMuitosParaMuitos(entity, dto);

        // O Hibernate, ao final do méthod @Transactional, vai perceber que
        // as listas mudaram e vai fazer os INSERTs nas tabelas de junção sozinho!
        return mapper.toDto(entity);
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
                .map(mapper::toDto);
    }

    @Override
    @Transactional
    public ServidorResponseDTO update(Integer id, ServidorRequestDTO dto) {
        // 1. Busca a entidade existente no banco (Entity em estado 'Managed' pelo Hibernate)
        Servidor entity = servidorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));

        // Chama a validação antes de atualizar
        beforeUpdate(dto, entity);

        // Atualiza os dados básicos e relacionamentos N:1 mapeados usando MapStruct
        mapper.updateEntityFromDTO(entity, dto);

        // Reassocia as coleções N para N (Sistemas, Aliases, Procuradores) tratadas via Service
        associarRelacoesMuitosParaMuitos(entity, dto);

        return mapper.toDto(servidorRepository.save(entity));
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

    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    @Override
    protected void beforeCreate(ServidorRequestDTO dto) {
        if (servidorRepository.existsByCpf(dto.cpf().trim())) {
            throw new BusinessException("CPF (" + dto.cpf() + ") já cadastrado");
        }

        if (servidorRepository.existsByMatricula(dto.matricula().trim())) {
            throw new BusinessException("Matrícula (" + dto.matricula() + ") já cadastrada.");
        }
    }

    @Override
    protected void beforeUpdate(ServidorRequestDTO dto, Servidor existingServidor) {
        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR o CPF
        if (!existingServidor.getCpf().equalsIgnoreCase(dto.cpf())) {
            if (servidorRepository.existsByCpf(dto.cpf())) {
                throw new BusinessException("Este CPF (" + dto.cpf() + ") já está sendo usado por outro servidor.");
            }
        }

        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR a matrícula
        if (!existingServidor.getMatricula().equalsIgnoreCase(dto.matricula())) {
            if (servidorRepository.existsByMatricula(dto.matricula())) {
                throw new BusinessException("Esta matrícula (" + dto.matricula() + ") já está sendo usada por outro servidor.");
            }
        }
    }

    @Override
    protected void beforeDelete(Servidor entity) {
        // Programação Defensiva: Verifica se o servidor tem um status associado para evitar NullPointerException
        if (entity.getStatus() == null) {
            throw new BusinessException("Não é possível excluir um servidor sem status definido");
        }
        // Compara a descrição ignorando maiúsculas e minúsculas
        if (!entity.getStatus().getDescricao().equalsIgnoreCase("Inativo")) {
            throw new BusinessException(
                    "O servidor só pode ser removido se o status for 'INATIVO'. " +
                            "Status atual: " + entity.getStatus().getDescricao().toUpperCase()
            );
        }

    }
}
