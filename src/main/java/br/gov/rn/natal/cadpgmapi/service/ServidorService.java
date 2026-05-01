package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.ServidorMapper;
import br.gov.rn.natal.cadpgmapi.models.ServidorShadowProjection;
import br.gov.rn.natal.cadpgmapi.repository.AliasRepository;
import br.gov.rn.natal.cadpgmapi.repository.ProcuradorRepository;
import br.gov.rn.natal.cadpgmapi.repository.ServidorRepository;
import br.gov.rn.natal.cadpgmapi.repository.SistemaRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

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
    @Auditable(action = AuditAction.INSERT, entity = "Servidor")
    public ServidorResponseDTO create(ServidorRequestDTO dto) {
        // 1. As validações blindadas
        beforeCreate(dto);
        // 4. NOVO CADASTRO
        // Converte o DTO para Entidade
        Servidor entity = mapper.toEntity(dto);

        // SALVA PRIMEIRO! (Isso gera o ID do Servidor no banco de dados)
        // Agora a entidade está "Managed" e tem um ID válido.
        entity = servidorRepository.save(entity);

        // Associa as relações NN (Sistemas, Aliases, Procuradores)
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
    @Auditable(action = AuditAction.UPDATE, entity = "Servidor")
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
        // 1. Validação de CPF (Bloqueia se existir ativo ou excluído)
        Optional<ServidorShadowProjection> shadowCpf = servidorRepository.checkCpfStatus(dto.cpf().trim());
        if (shadowCpf.isPresent()) {
            throw new BusinessException(
                    "Este CPF (<strong>" + cpfFormat(dto.cpf()) + "</strong>)</br>já está em uso em outro cadastro."
            );
        }

        // 2. Validação da Matrícula (Bloqueia se existir ativa ou excluída)
        Optional<ServidorShadowProjection> shadowMatricula = servidorRepository.checkMatriculaStatus(dto.matricula().trim());
        if (shadowMatricula.isPresent()) {
            throw new BusinessException(
                    "Esta Matrícula (<strong>" + dto.matricula() + "</strong>)</br>" +
                            "já está em uso em outro cadastro."
            );
        }

        // 3. Validação de E-mail Pessoal
        servidorRepository.checkEmailPessoalStatus(dto.emailPessoal().trim())
                .ifPresent(s -> {
                    throw new BusinessException("Este E-mail (<strong>" + dto.emailPessoal() + "</strong>)</br>" +
                            "já está em uso em outro cadastro.");
                });

        // 4. Validação de E-mail Institucional (Só valida se for informado)
        if (dto.emailInstitucional() != null && !dto.emailInstitucional().isBlank()) {
            servidorRepository.checkEmailInstitucionalStatus(dto.emailInstitucional().trim())
                    .ifPresent(s -> {
                        throw new BusinessException("Este E-mail (<strong>" + dto.emailInstitucional() + "</strong>)</br>" +
                                "já está em uso em outro cadastro.");
                    });
        }
    }

    @Override
    protected void beforeUpdate(ServidorRequestDTO dto, Servidor existingServidor) {
        // 1. Só valida se MUDOU o CPF na tela
        if (!existingServidor.getCpf().equalsIgnoreCase(dto.cpf().trim())) {
            Optional<ServidorShadowProjection> shadowCpf = servidorRepository.checkCpfStatus(dto.cpf().trim());
            if (shadowCpf.isPresent()) {
                throw new BusinessException(
                        "Este CPF (<strong>" + cpfFormat(dto.cpf()) + "</strong>)</br>já está em uso em outro cadastro."
                );
            }
        }

        // 2. Só valida se MUDOU a Matrícula na tela
        if (!existingServidor.getMatricula().equalsIgnoreCase(dto.matricula().trim())) {
            Optional<ServidorShadowProjection> shadowMatricula = servidorRepository.checkMatriculaStatus(dto.matricula().trim());
            if (shadowMatricula.isPresent()) {
                throw new BusinessException(
                        "Esta Matrícula (<strong>" + dto.matricula() + "</strong>)</br>já está em uso em outro cadastro."
                );
            }
        }

        // Validação de E-mail Pessoal
        if (!existingServidor.getEmailPessoal().equalsIgnoreCase(dto.emailPessoal().trim())) {
            servidorRepository.checkEmailPessoalStatus(dto.emailPessoal().trim())
                    .ifPresent(s -> {
                        throw new BusinessException("Este E-mail (<strong>" + dto.emailPessoal() + "</strong>)</br>" +
                                "já está em uso em outro cadastro."
                        );
                    });
        }

        // Validação de E-mail Institucional
        String newEmailInst = dto.emailInstitucional() != null ? dto.emailInstitucional().trim() : "";
        String oldEmailInst = existingServidor.getEmailInstitucional() != null
                ? existingServidor.getEmailInstitucional() : "";

        if (!newEmailInst.isBlank() && !newEmailInst.equalsIgnoreCase(oldEmailInst)) {
            servidorRepository.checkEmailInstitucionalStatus(newEmailInst)
                    .ifPresent(s -> {
                        throw new BusinessException("Este E-mail (<strong>" + newEmailInst + "</strong>)</br>" +
                                "já está em uso em outro cadastro.");
                    });
        }
    }

    @Override
    protected void beforeDelete(Servidor entity) {
        // Programação Defensiva: Verifica se o servidor tem um status associado para evitar NullPointerException
        if (entity.getStatus().equals(null)) {
            throw new BusinessException("Não é possível excluir um servidor sem status definido");
        }
        // Compara a descrição ignorando maiúsculas e minúsculas
        if (!entity.getStatus().getDescricao().equalsIgnoreCase("Inativo")) {
            throw new BusinessException("Somente Servidor com Status (<strong>'INATIVO'</strong>) pode ser removido." +
                    "Status atual: (<strong>'" + entity.getStatus().getDescricao().toUpperCase() + "'</strong>)."
            );
        }

    }

    // MÉTODOS PRIVADOS
    private ServidorResponseDTO reativateServidor(Servidor entity, ServidorRequestDTO dto) {
        // 1. Removemos as marcas de exclusão
        entity.setExcluded(false);
        entity.setExcludedDate(null);

        // 2. Atualizamos os dados com o que veio no DTO (pode ter mudado telefone, etc)
        mapper.updateEntityFromDTO(entity, dto);

        // 3. Salva para persistir a volta ao mundo dos ativos
        entity = servidorRepository.save(entity);

        // 4. Atualiza as relações N:N
        associarRelacoesMuitosParaMuitos(entity, dto);

        return mapper.toDto(entity);
    }

    private String cpfFormat(String cpf) {
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }
}
