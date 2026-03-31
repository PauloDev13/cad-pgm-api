package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.AliasRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.AliasResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Alias;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.AliasMapper;
import br.gov.rn.natal.cadpgmapi.repository.AliasRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AliasService extends BaseGenericService<Alias, AliasRequestDTO, AliasResponseDTO, Integer> {
    private final AliasRepository aliasRepository;

    // Construtor
    public AliasService(AliasRepository repository, AliasMapper mapper) {
        super(repository, mapper);
        this.aliasRepository = repository;
    }

    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    @Override
    protected void beforeCreate(AliasRequestDTO dto) {
        if (aliasRepository.existsByEmail(dto.email().trim())) {
            throw new BusinessException("Já existe um Alias cadastrado como este e-mail " + dto.email());
        }
    }

    @Override
    protected void beforeUpdate(AliasRequestDTO dto, Alias existingAlias) {
        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR o e-mail
        if (!existingAlias.getEmail().equalsIgnoreCase(dto.email())) {
            if (aliasRepository.existsByEmail(dto.email())) {
                throw new BusinessException("Este e-mail já está sendo usado por outro alias.");
            }
        }
    }


    @Transactional(readOnly = true)
    public Page<AliasResponseDTO> findByFilterEmail(String filter, Pageable pageable) {
        if (filter == null || filter.trim().isEmpty()) {
            return super.findAll(pageable); // Reaproveita o método do BaseCrudService!
        }

        return aliasRepository.findByEmailContainingIgnoreCase(filter.trim(), pageable)
                .map(mapper::toDto); // Usamos o mapper genérico da classe pai
    }
}
