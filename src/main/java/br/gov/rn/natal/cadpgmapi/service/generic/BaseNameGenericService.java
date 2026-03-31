package br.gov.rn.natal.cadpgmapi.service.generic;

import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import br.gov.rn.natal.cadpgmapi.repository.generic.BaseNameRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

// Service usado ÚNICA E EXCLUSIVAMENTE para as entidades que têem o atributo NOME
public abstract class BaseNameGenericService<E, Req, Res, ID> extends BaseGenericService<E, Req, Res, ID> {
    protected final BaseNameRepository<E, ID> nameRepository;

    public BaseNameGenericService(
            BaseNameRepository<E, ID> repository,
            BaseMapper<E, Req, Res> mapper) {
        super(repository, mapper);
        this.nameRepository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Res> findByFilterName(String filter, Pageable pageable) {
        if (filter == null || filter.trim().isEmpty()) {
            return super.findAll(pageable); // Reaproveita o método do BaseCrudService!
        }

        return nameRepository.findByNomeContainingIgnoreCase(filter.trim(), pageable)
                .map(mapper::toDto); // Usamos o mapper genérico da classe pai
    }
}
