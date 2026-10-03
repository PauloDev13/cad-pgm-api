package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.dto.request.FeriadoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.FeriadoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Feriado;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.mapper.FeriadoMapper;
import br.gov.rn.natal.cadpgmapi.repository.FeriadoRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import br.gov.rn.natal.cadpgmapi.utils.EntityChangeEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeriadoService extends BaseNameGenericService<Feriado, FeriadoRequestDTO, FeriadoResponseDTO, Integer> {

    private final FeriadoRepository feriadoRepository;

    public FeriadoService(
            FeriadoRepository repository,
            FeriadoMapper mapper,
            ApplicationEventPublisher eventPublisher) {
        super(repository, mapper, eventPublisher);
        feriadoRepository = repository;
    }

    // ================================================================
    // CONSULTAS COM CACHE PARA O RELATÓRIO DE FOLHA DE PONTO
    // ================================================================

    @Transactional(readOnly = true)
    @Cacheable(value = "feriadosCache", key = "#mes != null ? #mes : 'todos'")
    public List<FeriadoResponseDTO> findAtivos(Integer mes) {
        List<Feriado> feriados;

        if (mes != null) {
            String mesPrefix = String.format("%02d-", mes);
            feriados = feriadoRepository.findAtivosByMes(mesPrefix);
        } else {
            feriados = feriadoRepository.findAllAtivos();
        }
        return mapper.toDtoList(feriados);
    }

    // ================================================================
    // ATUALIZAÇÃO ATÔMICA DO CHECKBOX (STATUS ATIVO)
    // ================================================================

    @Transactional
    @Auditable(action = AuditAction.UPDATE)
    @CacheEvict(value = "feriadosCache", allEntries = true)
    public FeriadoResponseDTO toggleStatus(Integer id, Boolean novoStatus) {
        Feriado feriado = feriadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feriado não encontrado para alteração de status"));

        feriado.setAtivo(novoStatus);

        Feriado saved = feriadoRepository.save(feriado);

        eventPublisher.publishEvent(new EntityChangeEvent(saved));

        return mapper.toDto(saved);
    }

    // ================================================================
    // REGRAS DE NEGÓCIO E VALIDAÇÕES (HOOKS DO BASE SERVICE)
    // ================================================================

    @Override
    protected void beforeCreate(FeriadoRequestDTO dto) {
        if (feriadoRepository.existsByData(dto.data())) {
            throw new BusinessException("Já existe um <strong>Feriado/Ponto Facultativo</strong> cadastrado para a data (<strong>"
                    + dto.data() + "</strong>).");
        }
    }
    @Override
    protected void beforeUpdate(FeriadoRequestDTO dto, Feriado existingFeriado) {
        if (!existingFeriado.getData().equals(dto.data())) {
            if (feriadoRepository.existsByDataAndIdNot(dto.data(), existingFeriado.getId())) {
                throw new BusinessException("Já existe outro <strong>Feriado/Ponto Facultativo</strong> cadastrado para a data (<strong>"
                        + dto.data() + "</strong>).");
            }
        }
    }
}
