package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.ProcuradorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Procurador;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.ProcuradorMapper;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import br.gov.rn.natal.cadpgmapi.repository.ProcuradorRepository;
import br.gov.rn.natal.cadpgmapi.repository.generic.BaseNameRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProcuradorService extends
        BaseNameGenericService<Procurador, ProcuradorRequestDTO, ProcuradorResponseDTO, Integer> {

    // Construtor
    public ProcuradorService(ProcuradorRepository repository, ProcuradorMapper mapper) {
        super(repository, mapper);
    }
}
