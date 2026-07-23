package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.SistemaRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SistemaResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Sistema;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.SistemaMapper;
import br.gov.rn.natal.cadpgmapi.repository.SistemaRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SistemaService extends BaseNameGenericService<
        Sistema, SistemaRequestDTO, SistemaResponseDTO, Integer>
{
    private final SistemaRepository sistemaRepository;

    // Construtor
    public SistemaService(
            SistemaRepository repository,
            SistemaMapper mapper,
            ApplicationEventPublisher eventPublisher) {
        super(repository, mapper, eventPublisher);
        this.sistemaRepository = repository;
    }

    // ================================================================
    // MÉTODOS SOBRESCRITOS EXCLUSIVAMENTE PARA GERENCIAMENTO DO CACHE
    // ================================================================

    // CACHE DA LISTA DE DROPDOWNS (Sobrescrevendo o método avô)
    @Override
    @Cacheable(value = "sistemasCache")
    public List<SistemaResponseDTO> findAllSelect() {
        return super.findAllSelect();
    }

    // ================================================================
    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    // ================================================================
    @Override
    protected void beforeCreate(SistemaRequestDTO dto) {
        if (sistemaRepository.existsByNome(dto.nome().trim())) {
            throw new BusinessException("Já existe um <strong>Sistema</strong> cadastrado como " +
                    "(<strong>" + dto.nome() +"</strong>).");
        }
    }

    @Override
    protected void beforeUpdate(SistemaRequestDTO dto, Sistema existingSistema) {
        // Só valida duplicidade se o usuário estiver de fato tentando MUDAR o e-mail
        if (!existingSistema.getNome().equalsIgnoreCase(dto.nome())) {
            if (sistemaRepository.existsByNome(dto.nome())) {
                throw new BusinessException("Este <strong>Sistema</strong> (<strong>" + dto.nome() +
                        "</strong>) já foi cadastrado");
            }
        }
    }
}
