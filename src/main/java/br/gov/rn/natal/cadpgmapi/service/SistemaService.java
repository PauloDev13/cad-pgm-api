package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.SistemaRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SistemaResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Sistema;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.SistemaMapper;
import br.gov.rn.natal.cadpgmapi.repository.SistemaRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import org.springframework.stereotype.Service;

@Service
public class SistemaService extends BaseNameGenericService<
        Sistema, SistemaRequestDTO, SistemaResponseDTO, Integer>
{
    private final SistemaRepository sistemaRepository;

    // Construtor
    public SistemaService(SistemaRepository repository, SistemaMapper mapper) {
        super(repository, mapper);
        this.sistemaRepository = repository;
    }

    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    @Override
    protected void beforeCreate(SistemaRequestDTO dto) {
        if (sistemaRepository.existsByNome(dto.nome().trim())) {
            throw new BusinessException("Já existe um Sistema cadastrado como " + dto.nome());
        }
    }

    @Override
    protected void beforeUpdate(SistemaRequestDTO dto, Sistema existingSistema) {
        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR o e-mail
        if (!existingSistema.getNome().equalsIgnoreCase(dto.nome())) {
            if (sistemaRepository.existsByNome(dto.nome())) {
                throw new BusinessException("Este Sistema (" + dto.nome() + ") já foi cadastrado");
            }
        }
    }
}
