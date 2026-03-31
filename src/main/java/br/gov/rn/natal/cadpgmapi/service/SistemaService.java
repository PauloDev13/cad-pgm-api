package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.SistemaRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SistemaResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Sistema;
import br.gov.rn.natal.cadpgmapi.mapper.SistemaMapper;
import br.gov.rn.natal.cadpgmapi.repository.SistemaRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import org.springframework.stereotype.Service;

@Service
public class SistemaService extends BaseNameGenericService<
        Sistema, SistemaRequestDTO, SistemaResponseDTO, Integer>
{
    // Construtor
    public SistemaService(SistemaRepository repository, SistemaMapper mapper) {
        super(repository, mapper);
    }
}
