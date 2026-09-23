package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.dto.request.ProcuradorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorSelectDTO;
import br.gov.rn.natal.cadpgmapi.entity.Procurador;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.mapper.ProcuradorMapper;
import br.gov.rn.natal.cadpgmapi.repository.ProcuradorRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseNameGenericService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
@Slf4j
@Service
public class ProcuradorService extends
        BaseNameGenericService<Procurador, ProcuradorRequestDTO, ProcuradorResponseDTO, Integer> {

    private final ProcuradorRepository procuradorRepository;

    // Construtor
    public ProcuradorService(
            ProcuradorRepository repository,
            ProcuradorMapper mapper,
            ApplicationEventPublisher eventPublisher) {
        super(repository, mapper, eventPublisher);
        this.procuradorRepository = repository;
    }

    /**
     * Retorna a lista de procuradores cujos certificados digitais
     * vencem em 7 dias ou menos, incluindo os que já se encontram vencidos.
     */
    @Transactional(readOnly = true)
    public List<ProcuradorResponseDTO> listarCertificadosProximosDoVencimento() {
        LocalDate hoje = LocalDate.now();
        LocalDate limiteVencimento = hoje.plusDays(7);

        return repository.findAll().stream()
                .filter(p -> p.getDataExpiracao() != null)
                .filter(p -> {
                    LocalDate expiracao = p.getDataExpiracao().toLocalDate();
                    // isAfter retorna false para datas anteriores ou iguais ao limite estipulado
                    return !expiracao.isBefore(hoje) && !expiracao.isAfter(limiteVencimento);
                })
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProcuradorSelectDTO> listarProcuradoresSelect() {
        return procuradorRepository.findAllToCombo();
    }

    // SÓ REGRA DE NEGÓCIO, ZERO CÓDIGO DE INFRAESTRUTURA
    @Override
    protected void beforeCreate(ProcuradorRequestDTO dto) {
        if (procuradorRepository.existsByNome(dto.nome().trim())) {
            throw new BusinessException("Já existe um <strong>Procurador</strong> cadastrado como " +
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
