package br.gov.rn.natal.cadpgmapi.service.generic;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.audit.utils.AuditDiffUtil;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class BaseGenericService<E, Req, Res, ID> {
    protected final JpaRepository<E, ID> repository;
    protected final BaseMapper<E, Req, Res> mapper;

    @PersistenceContext
    protected EntityManager entityManager;

    // Construtor
    protected BaseGenericService(
            JpaRepository<E, ID> repository,
            BaseMapper<E, Req, Res> mapper

    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // HOOKS (GANCHOS)
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

        // O PAI CHAMA O NOVO GANCHO AQUI!
        // Dá a chance do filho fazer associações Muitos-para-Muitos
        afterSave(entity, dto);

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
                .orElseThrow(() -> new ResourceNotFoundException("Registro mão encontrado"));
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE)
    public Res update(ID id, Req dto) {

        // Busca o registro do banco ou lança erro se não existir
        E existingEntity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado"));

        // 1. Tira a FOTO ANTIGA
        Res oldSnapshot = mapper.toDto(existingEntity);

        // 2. Passa pelos ganchos e atualiza
        beforeUpdate(dto, existingEntity);
        // A. Usa o méthod criado no BaseMapper para atualizar os campos da entidade buscada
        mapper.updateEntityFromDTO(existingEntity, dto);
        // B. O gancho atua aqui também, já com os dados novos do DTO aplicados!
        beforeSave(existingEntity);

        // 3. Salva e sincroniza
        existingEntity = repository.save(existingEntity);
        // A. Garante que os dados foram pro banco antes do próximo passo
        repository.flush();
        // B. Garante nomes das chaves estrangeiras
        entityManager.refresh(existingEntity);
        // C. Gancho para relações N:N, refresh e auditoria de comparação
        afterSave(existingEntity, dto);

        // 4. Tira a FOTO NOVA
        Res newSnapshot = mapper.toDto(existingEntity);

        // 5. Gera o Log de Diferenças Universal
        String diffLog = AuditDiffUtil.generateDiff(oldSnapshot, newSnapshot);

        if (!diffLog.isBlank()) {
            AuditContextHolder.setLogDetalhes("Dados atualizados: " + diffLog);
        } else {
            AuditContextHolder.setLogDetalhes("Nenhuma alteração detectada nos dados.");
        }

        // O retorno já virá "hidratado" se o filho usou o entityManager.refresh no afterSave
        return newSnapshot;
    }

    @Transactional
    @Auditable(action = AuditAction.DELETE)
    public void delete(ID id) {
        // Centralizamos a busca e a exceção de 404 (uma única ida ao banco)
        E existingEntity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para exclusão."));

        // 2. Chamamos o gancho. Se o filho lançar uma BusinessException, a exclusão é abortada!
        beforeDelete(existingEntity);

        // 3. Se o gancho passar em silêncio, deletamos a entidade
        repository.delete(existingEntity);

        // 4. Gancho DEPOIS de excluir (Limpar caches, disparar emails, etc.)
        afterDelete(existingEntity);
    }
}
