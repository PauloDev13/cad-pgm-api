package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.ProcuradorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Procurador;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.ProcuradorMapper;
import br.gov.rn.natal.cadpgmapi.repository.ProcuradorRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import org.springframework.stereotype.Service;

@Service
public class ProcuradorService extends
        BaseNameGenericService<Procurador, ProcuradorRequestDTO, ProcuradorResponseDTO, Integer> {

    private final ProcuradorRepository procuradorRepository;

    // Construtor
    public ProcuradorService(
            ProcuradorRepository repository,
            ProcuradorMapper mapper) {
        super(repository, mapper);
        this.procuradorRepository = repository;
    }

    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    @Override
    protected void beforeCreate(ProcuradorRequestDTO dto) {
        if (procuradorRepository.existsByNome(dto.nome().trim())) {
            throw new BusinessException("Já existe um <strong>Procurador<strong> cadastrado como " +
                    "(<strong>" + dto.nome() + "</strong>).");
        }
    }

    @Override
    protected void beforeUpdate(ProcuradorRequestDTO dto, Procurador existingProcurador) {
        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR o e-mail
        if (!existingProcurador.getNome().equalsIgnoreCase(dto.nome())) {
            if (procuradorRepository.existsByNome(dto.nome())) {
                throw new BusinessException("Este <strong>Procurador</strong> (<strong>" + dto.nome() +
                        "</strong>) já foi cadastrado");
            }
        }
    }
}
