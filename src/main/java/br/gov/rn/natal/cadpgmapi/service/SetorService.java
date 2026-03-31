package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.SetorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SetorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Setor;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.SetorMapper;
import br.gov.rn.natal.cadpgmapi.repository.SetorRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import org.springframework.stereotype.Service;

@Service
public class SetorService extends BaseNameGenericService<Setor, SetorRequestDTO, SetorResponseDTO, Integer> {
    private final SetorRepository setorRepository;

    // Construtor
    public SetorService(SetorRepository repository, SetorMapper mapper) {
        super(repository, mapper);
        this.setorRepository = repository;
    }

    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    @Override
    protected void beforeCreate(SetorRequestDTO dto) {
        if (setorRepository.existsByNome(dto.nome().trim())) {
            throw new BusinessException("Já existe um Setor cadastrado como " + dto.nome());
        }
    }

    @Override
    protected void beforeUpdate(SetorRequestDTO dto, Setor existingSetor) {
        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR o e-mail
        if (!existingSetor.getNome().equalsIgnoreCase(dto.nome())) {
            if (setorRepository.existsByNome(dto.nome())) {
                throw new BusinessException("Este Setor (" + dto.nome() + ") já foi cadastrado");
            }
        }
    }
}
