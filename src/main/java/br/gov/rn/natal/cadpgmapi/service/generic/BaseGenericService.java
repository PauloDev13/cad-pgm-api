package br.gov.rn.natal.cadpgmapi.service.generic;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.audit.utils.AuditDiffUtil;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import br.gov.rn.natal.cadpgmapi.utils.EntityChangeEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class BaseGenericService<E, Req, Res, ID> {
    protected final JpaRepository<E, ID> repository;
    protected final BaseMapper<E, Req, Res> mapper;

    // Injetamos o publicador de eventos do Spring
    protected final ApplicationEventPublisher eventPublisher;

    @PersistenceContext
    protected EntityManager entityManager;

    // Construtor
    protected BaseGenericService(
            JpaRepository<E, ID> repository,
            BaseMapper<E, Req, Res> mapper,
            ApplicationEventPublisher eventPublisher

    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
    }

    /* ============================================
                HOOKS (GANCHOS)
    * =============================================*/
    // Eles ficam vazios por padrão. Os filhos sobrescrevem se precisarem.
    protected void beforeCreate(Req dto) {}
    protected void beforeUpdate(Req dto, E existingEntity) {}
    protected void beforeDelete(E entity) {}
    protected void afterDelete(E entity) {}
    // Roda antes de ir para banco, mas DEPOIS do mapper!
    protected void beforeSave(E entity) {}
    // Roda DEPOIS do insert no banco, quando a entidade já tem ID!
    protected void afterSave(E entity, Req dto) {}

    @Transactional
    @Auditable(action = AuditAction.INSERT)
    public Res create(Req dto) {
        // O pai chama o gancho. Se o filho sobrescreveu e lançar erro, a execução para aqui!
        beforeCreate(dto);

        E entity = mapper.toEntity(dto);
        // O pai dá a chance do filho alterar a entidade (ex: criptografar senha)
        beforeSave(entity);

        // Salva e garante o ID gerado no banco
        entity = repository.save(entity);

        afterSave(entity, dto);

        // Avisa que houve mudança!
        eventPublisher.publishEvent(new EntityChangeEvent(entity));
        return mapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<Res> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<Res> findAllSelect() {
        return mapper.toDtoList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public Res findById(ID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado"));
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE)
    public Res update(ID id, Req dto) {

        // Busca o registro do banco ou lança erro se não existir
        E existingEntity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado"));

        // 1. Tira o SNAPSHOT ANTIGO (estado atual da entidade antes da alteração)
        Res oldSnapshot = mapper.toDto(existingEntity);

        // 2. Passa pelos ganchos e atualiza
        beforeUpdate(dto, existingEntity);
        // A. Usa o método criado no BaseMapper para atualizar os campos da entidade buscada
        mapper.updateEntityFromDTO(existingEntity, dto);
        // B. O gancho atua aqui também, já com os dados novos do DTO aplicados!
        beforeSave(existingEntity);

        // 3. Salva e sincroniza
        existingEntity = repository.save(existingEntity);
        // A. Garante que os dados foram para o banco antes do próximo passo
        repository.flush();
        // B. Garante nomes das chaves estrangeiras
        entityManager.refresh(existingEntity);
        // C. Gancho para relações N:N, refresh e auditoria de comparação
        afterSave(existingEntity, dto);

        // 4. Tira o SNAPSHOT NOVO (estado atualizado para o diff de auditoria)
        Res newSnapshot = mapper.toDto(existingEntity);

        // CORREÇÃO DE BUG (item 9): em versões anteriores o diff era gerado DUAS vezes —
        //  1) aqui no método base e 2) lá no afterSave sobrescrito de cada service (ex.:
        //     ServidorService gera o diff com o prefixo "ATUALIZAÇÃO:" e o armazena no
        //     AuditContextHolder). A segunda geração SOBRESCREVIA os detalhes customizados
        //     do service e desperdiçava processamento.
        // A regra ficou: se o afterSave JÁ forneceu os detalhes de auditoria, respeitamos;
        // só calculamos o diff aqui quando o afterSave não fez isso (fallback p/ entidades
        // simples como Cargo e Status, que não sobrescrevem o hook).
        if (AuditContextHolder.getLogDetalhes() == null) {
            String diffLog = AuditDiffUtil.generateDiff(oldSnapshot, newSnapshot);

            if (!diffLog.isBlank()) {
                AuditContextHolder.setLogDetalhes("ATUALIZAÇÃO: " + diffLog);
            } else {
                AuditContextHolder.setLogDetalhes("Nenhuma atualização detectada nos dados.");
            }
        }

        // Avisa que houve mudança!
        eventPublisher.publishEvent(new EntityChangeEvent(existingEntity));

        // O retorno já virá "hidratado" se o filho usou o entityManager.refresh no afterSave
        return newSnapshot;
    }

    @Transactional
    @Auditable(action = AuditAction.DELETE)
    public void delete(ID id) {
        // Centralizamos a busca e a exceção de 404 (uma única ida ao banco)
        E existingEntity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para exclusão."));

        // 1. Nome da Entidade (ex: Servidor)
        AuditContextHolder.setEntityName(existingEntity.getClass().getSimpleName());

        // 2. Converte para DTO, extrai o ID Amigável (CPF/E-mail) e guarda no Contexto
        String friendlyId = AuditDiffUtil.extractFriendlyId(mapper.toDto(existingEntity));
        AuditContextHolder.setFriendlyId(friendlyId);

        // 3. Chamamos o gancho. Se o filho lançar uma BusinessException, a exclusão é abortada!
        beforeDelete(existingEntity);

        // 4. Chama o gancho que está sobrescrito no service filho que aplica o Soft Delete
        // ao invés de chamar o Soft Delete declarado no @SQLDelete da entidade Servidor.
//        repository.delete(existingEntity);
        performDelete(existingEntity);

        // 5. Gancho DEPOIS de excluir (Limpar caches, disparar emails, etc.)
        afterDelete(existingEntity);

        // Avisa que houve mudança!
        eventPublisher.publishEvent(new EntityChangeEvent(existingEntity));
    }

    // Por padrão, todos os services usarão o delete normal do Hibernate
    protected void performDelete(E entity) {
        repository.delete(entity);
    }
}
