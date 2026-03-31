package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.SetorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SetorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Setor;
import br.gov.rn.natal.cadpgmapi.mapper.SetorMapper;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import br.gov.rn.natal.cadpgmapi.repository.SetorRepository;
import br.gov.rn.natal.cadpgmapi.repository.generic.BaseNameRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetorService extends BaseNameGenericService<Setor, SetorRequestDTO, SetorResponseDTO, Integer> {

    // Construtor
    public SetorService(SetorRepository repository, SetorMapper mapper) {
        super(repository, mapper);
    }
}
