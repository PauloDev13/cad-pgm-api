package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.VinculoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.VinculoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Vinculo;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.VinculoMapper;
import br.gov.rn.natal.cadpgmapi.repository.VinculoRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import org.springframework.stereotype.Service;

@Service
public class VinculoService extends BaseNameGenericService<Vinculo, VinculoRequestDTO, VinculoResponseDTO, Integer> {
    private final VinculoRepository vinculoRepository;
    
    protected VinculoService(VinculoRepository repository, VinculoMapper mapper) {
        super(repository, mapper);
        this.vinculoRepository = repository;
    }

    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    @Override
    protected void beforeCreate(VinculoRequestDTO dto) {
        if (vinculoRepository.existsByNome(dto.nome().trim())) {
            throw new BusinessException("Já existe um Vinculo cadastrado como " + dto.nome());
        }
    }

    @Override
    protected void beforeUpdate(VinculoRequestDTO dto, Vinculo existingVinculo) {
        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR o e-mail
        if (!existingVinculo.getNome().equalsIgnoreCase(dto.nome())) {
            if (vinculoRepository.existsByNome(dto.nome())) {
                throw new BusinessException("Este Vinculo (" + dto.nome() + ") já foi cadastrado");
            }
        }
    }
}
