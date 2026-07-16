package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.SetorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SetorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Setor;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.SetorMapper;
import br.gov.rn.natal.cadpgmapi.repository.SetorRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetorService extends BaseNameGenericService<Setor, SetorRequestDTO, SetorResponseDTO, Integer> {
    private final SetorRepository setorRepository;

    // Construtor
    public SetorService(SetorRepository repository, SetorMapper mapper) {
        super(repository, mapper);
        this.setorRepository = repository;
    }

    // ================================================================
    // MÉTODOS SOBRESCRITOS EXCLUSIVAMENTE PARA GERENCIAMENTO DO CACHE
    // ================================================================

    // CACHE DA LISTA DE DROPDOWNS (Sobrescrevendo o método avô)
    @Override
    @Cacheable(value = "setoresCache")
    public List<SetorResponseDTO> findAllSelect(){
        return super.findAllSelect();
    }

    // CRIAÇÃO: Esvazia a gaveta "setoresCache"
    @Override
    @CacheEvict(value = "setoresCache", allEntries = true)
    public SetorResponseDTO create(SetorRequestDTO dto) {
        return super.create(dto);
    }

    // ATUALIZAÇÃO: Esvazia a gaveta "setoresCache"
    @Override
    @CacheEvict(value = "setoresCache", allEntries = true)
    public SetorResponseDTO update(Integer id, SetorRequestDTO dto) {
        return super.update(id, dto);
    }

    // EXCLUSÃO: Esvazia a gaveta "setoresCache"
    @Override
    @CacheEvict(value = "setoresCache", allEntries = true)
    public void delete(Integer id) {
        super.delete(id);
    }

    // ================================================================
    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    // ================================================================
    @Override
    protected void beforeCreate(SetorRequestDTO dto) {
        if (setorRepository.existsByNome(dto.nome().trim())) {
            throw new BusinessException("Já existe um <strong>Setor</strong> cadastrado como " +
                    "(<strong>" + dto.nome() +"</strong>).");
        }
    }

    @Override
    protected void beforeUpdate(SetorRequestDTO dto, Setor existingSetor) {
        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR o e-mail
        if (!existingSetor.getNome().equalsIgnoreCase(dto.nome())) {
            if (setorRepository.existsByNome(dto.nome())) {
                throw new BusinessException("Este <strong>Setor</strong> (<strong>" + dto.nome() +
                        "</strong>) já foi cadastrado");
            }
        }
    }
}
