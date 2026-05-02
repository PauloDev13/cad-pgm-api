package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
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
    private final EntityManager entityManager;

    // Construtor
    public ServidorService(
            ServidorRepository repository,
            ServidorMapper mapper,
            SistemaRepository sistemaRepository,
            AliasRepository aliasRepository,
            ProcuradorRepository procuradorRepository, EntityManager entityManager
    ){
        super(repository, mapper);
        this.servidorRepository = repository;
        this.sistemaRepository = sistemaRepository;
        this.aliasRepository = aliasRepository;
        this.procuradorRepository = procuradorRepository;
        this.entityManager = entityManager;
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

        // 2. Tira uma "foto" dos dados ANTIGOS convertendo para DTO Request
        // (Converter para DTO limpa as sujeiras do Hibernate e facilita a comparação)
//        ServidorRequestDTO oldSnapshot = mapper.toReqDto(entity);
        ServidorResponseDTO oldSnapshot = mapper.toDto(entity);

        // 3. Validações e Regras de Negócio
        beforeUpdate(dto, entity);

        // 4. Aplica as alterações na entidade
        mapper.updateEntityFromDTO(entity, dto);
        // Reassocia as coleções N para N (Sistemas, Aliases, Procuradores)
        associarRelacoesMuitosParaMuitos(entity, dto);

        // 3. A MÁGICA DA HIDRATAÇÃO ACONTECE AQUI
        // Primeiro empurramos a alteração pro banco para salvar as chaves estrangeiras novas
        servidorRepository.saveAndFlush(entity);

        // Agora mandamos o chefe do Hibernate buscar o registro inteiro de novo,
        // trazendo todos os nomes (Cargo, Setor, Status) preenchidos e reais!
        entityManager.refresh(entity);

        // 5. Tira uma "foto" dos dados NOVOS
        ServidorResponseDTO newSnapshot = mapper.toDto(entity);

        // 5. GERA O TEXTO DE AUDITORIA ("A Mágica")
        String detailsLog = generateAuditText(oldSnapshot, newSnapshot);

        AuditContextHolder.setLogDetalhes(detailsLog);

        // 7. Salva no banco e retorna
        return mapper.toDto(servidorRepository.save(entity));
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

    // Método que devolve um texto com as alterações realizadas no método Update
    private String generateAuditText(Object oldObject, Object newObject) {
        // Inicializa o motor do JaVers
        Javers javers = JaversBuilder.javers().build();

        // Faz a comparação mágica
        Diff diff = javers.compare(oldObject, newObject);
        // Se não mudou nada, retorna vazio
        if (!diff.hasChanges()) {
            return "Nenhuma alteração detectada.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Alterações realizadas: ");

        // Percorre cada campo que mudou e monta a frase
        for (ValueChange change : diff.getChangesByType(ValueChange.class)) {
            String field = change.getPropertyName();

            // Se no nome do campo vier "i", não adicione ao array
            if ("id".equalsIgnoreCase(field)) continue;

            Object oldValue = change.getLeft();
            Object newValue = change.getRight();

            builder.append(String.format("[%s: de '%s' para '%s'] ", field, oldValue, newValue));
        }

        return builder.toString().trim();

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
        // return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        // Formatação direta, simples e extremamente rápida
        return cpf.substring(0, 3) + "." +
                cpf.substring(3, 6) + "." +
                cpf.substring(6, 9) + "-" +
                cpf.substring(9, 11);
    }
}
