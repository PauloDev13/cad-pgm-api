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
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Transactional(readOnly = true)
    public List<ProcuradorSelectDTO> listarProcuradoresSelect() {
//        return procuradorRepository.findAllToCombo();
        return procuradorRepository.findAllToCombo()
                .stream()
                .toList();
    }

    /**
     * Retorna a lista de procuradores cujos certificados digitais
     * vencem exatamente daqui a 7 dias a contar da data atual.
     */
    @Transactional(readOnly = true)
    public List<ProcuradorResponseDTO> listarCertificadosProximosDoVencimento() {
        LocalDate dataAlvo = LocalDate.now().plusDays(7);
        LocalDateTime inicioDia = dataAlvo.atStartOfDay();
        LocalDateTime fimDia = dataAlvo.atTime(LocalTime.MAX);

        return procuradorRepository.findCertificadosExpirandoEntre(inicioDia, fimDia)
                .stream()
                .toList();
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
