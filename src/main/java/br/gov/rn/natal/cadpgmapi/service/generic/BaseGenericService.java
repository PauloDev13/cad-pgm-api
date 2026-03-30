package br.gov.rn.natal.cadpgmapi.service.generic;

import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseGenericService<E, Req, Res, ID> {
    protected final JpaRepository<E, ID> repository;
    protected final BaseMapper<E, Req, Res> mapper;

    protected BaseGenericService(
            JpaRepository<E, ID> repository,
            BaseMapper<E, Req, Res> mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public Res create(Req dto) {
        E entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<Res> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Res findById(ID id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Registro mão encontrado"));
    }

    @Transactional
    public Res update(ID id, Req dto) {
        // Busca o registro do banco ou lança erro se não existir
        E exitingEntity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado"));

        // Usa o méthod criado no BaseMapper para atualizar os campos da entidade buscada
        mapper.updateEntityFromDTO(exitingEntity, dto);

        // Salva e converte para DTO de resposta
        E updatedEntity = repository.save(exitingEntity);

        // Usa o méthod criado no BaseMapper para transformar a entidade salva em DTO
        // e retorna o DTO
        return mapper.toDto(updatedEntity);
    }

    @Transactional
    public void delete(ID id) {
        // Consulta se o registro existe no banco ou lança erro se não existir
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Registro não encontrado");
        }
        // Remove o registro do banco
        repository.deleteById(id);
    }
}
