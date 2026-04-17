package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.StatusRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.StatusResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Status;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.StatusMapper;
import br.gov.rn.natal.cadpgmapi.repository.StatusRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusService extends BaseGenericService<Status, StatusRequestDTO, StatusResponseDTO, Integer> {
    private final StatusRepository statusRepository;

    protected StatusService(StatusRepository repository, StatusMapper mapper) {
        super(repository, mapper);
        this.statusRepository = repository;
    }

    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    @Override
    protected void beforeCreate(StatusRequestDTO dto) {
        if (statusRepository.existsByDescricao(dto.descricao().trim())) {
            throw new BusinessException("Já existe um <strong>Status</strong> cadastrado como " +
                    "(<strong>" + dto.descricao() + "</strong>).");
        }
    }

    @Override
    protected void beforeUpdate(StatusRequestDTO dto, Status existingStatus) {
        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR o e-mail
        if (!existingStatus.getDescricao().equalsIgnoreCase(dto.descricao())) {
            if (statusRepository.existsByDescricao(dto.descricao())) {
                throw new BusinessException("Este <strong>Status<strong> (<strong>"+ dto.descricao() +
                        "</strong>) já está em uso.");
            }
        }
    }


    @Transactional(readOnly = true)
    public Page<StatusResponseDTO> findByFilterDescricao(String filter, Pageable pageable) {
        if (filter == null || filter.trim().isEmpty()) {
            return super.findAll(pageable); // Reaproveita o méthod do BaseCrudService!
        }

        return statusRepository.findByDescricaoContainingIgnoreCase(filter.trim(), pageable)
                .map(mapper::toDto); // Usamos o mapper genérico da classe pai
    }
}
