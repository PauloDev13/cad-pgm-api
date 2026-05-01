package br.gov.rn.natal.cadpgmapi.service.generic;

import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class BaseGenericService<E, Req, Res, ID> {
    protected final JpaRepository<E, ID> repository;
    protected final BaseMapper<E, Req, Res> mapper;

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
    // Roda antes de ir para banco, mas DEPOIS do mapper!
    protected void beforeSave(E entity) {}

    @Transactional
    @Auditable(action = AuditAction.INSERT)
    public Res create(Req dto) {
        // O pai chama o gancho. Se o filho sobrescreveu e lançar erro, a execução para aqui!
        beforeCreate(dto);

        E entity = mapper.toEntity(dto);
        // O pai dá a chance do filho alterar a entidade (ex: criptografar senha)
        beforeSave(entity);

        return mapper.toDto(repository.save(entity));
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
    @Auditable(action = AuditAction.INSERT)
    public Res update(ID id, Req dto) {
        // Busca o registro do banco ou lança erro se não existir
        E existingEntity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado"));

        // O pai chama o gancho e já entrega a entidade mastigada pro filho!
        beforeUpdate(dto, existingEntity);

        // Usa o méthod criado no BaseMapper para atualizar os campos da entidade buscada
        mapper.updateEntityFromDTO(existingEntity, dto);

        // O gancho atua aqui também, já com os dados novos do DTO aplicados!
        beforeSave(existingEntity);

        // Atualiza e salva
        E updatedEntity = repository.save(existingEntity);

        // Usa o méthod criado no BaseMapper para transformar a entidade salva em DTO
        // e retorna o DTO
        return mapper.toDto(updatedEntity);
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
    }
}
