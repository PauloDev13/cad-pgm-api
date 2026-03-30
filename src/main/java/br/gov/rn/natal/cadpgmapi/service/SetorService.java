package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.SetorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SetorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Setor;
import br.gov.rn.natal.cadpgmapi.mapper.SetorMapper;
import br.gov.rn.natal.cadpgmapi.repository.SetorRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetorService extends BaseGenericService<Setor, SetorRequestDTO, SetorResponseDTO, Integer> {
    private final SetorRepository setorRepository;
    public SetorService(SetorRepository setorRepository, SetorMapper mapper) {
        super(setorRepository, mapper);
        this.setorRepository = setorRepository;
    }

    @Transactional(readOnly = true)
    public Page<SetorResponseDTO> findByFilterName(String filter, Pageable pageable) {
        // Se a descrição vier nulo ou vazio, você pode optar por retornar todos os registros
        if (filter == null || filter.trim().isEmpty()) {
            return super.repository.findAll(pageable) // Reaproveita o findAll da classe pai!
                    .map(mapper::toDto);
        }

        // Usa o SetorRepository original para poder acessar o méthod de busca
        return setorRepository.findByNomeContainingIgnoreCase(filter.trim(), pageable)
                .map(mapper::toDto);
    }
}
