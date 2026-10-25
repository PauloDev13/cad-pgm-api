package br.gov.rn.natal.cadpgmapi.manutencao.service;

import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ForbiddenException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.*;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.*;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.*;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.*;
import br.gov.rn.natal.cadpgmapi.manutencao.repository.*;
import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import br.gov.rn.natal.cadpgmapi.security.SecurityRoles;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChamadoManutencaoService {

    private final ChamadoManutencaoRepository chamadoRepository;
    private final HistoricoChamadoRepository historicoRepository;
    private final SalaRepository salaRepository;
    private final CategoriaDemandaRepository categoriaRepository;
    private final TipoServicoRepository tipoServicoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    @Auditable(action = AuditAction.INSERT, entity = "Chamado de Manutenção")
    public ChamadoDetalheResponseDTO abrirChamado(AberturaChamadoRequestDTO dto) {
        Usuario solicitante = getUsuarioAutenticado();

        Sala sala = null;

        if (dto.salaId() != null) {
            sala = salaRepository.findById(dto.salaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sala selecionada não encontrada."));
            if (!sala.isAtivo()) {
                throw new BusinessException("A sala selecionada está inativa.");
            }
            if (sala.getAndar() != dto.andar()) {
                throw new BusinessException("A sala informada não pertence ao andar selecionado.");
            }
        } else {
            if (dto.localDescricao() == null || dto.localDescricao().trim().isBlank()) {
                throw new BusinessException("A descrição do local é obrigatória quando a opção 'Outro' for selecionada.");
            }
        }

        CategoriaDemanda categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria informada não encontrada."));
        if (!categoria.isAtivo()) {
            throw new BusinessException("A categoria selecionada encontra-se inativa.");
        }

        TipoServico tipoServico = tipoServicoRepository.findById(dto.tipoServicoId())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de serviço não encontrado."));
        if (!tipoServico.isAtivo()) {
            throw new BusinessException("O tipo de serviço selecionado encontra-se inativo.");
        }
        if (!tipoServico.getCategoria().getId().equals(categoria.getId())) {
            throw new BusinessException("O tipo de serviço informado não pertence à categoria selecionada.");
        }

        String protocolo = gerarProtocoloUnico();

        ChamadoManutencao chamado = ChamadoManutencao.builder()
                .protocolo(protocolo)
                .solicitante(solicitante)
                .andar(dto.andar())
                .sala(sala)
                .localDescricao(sala == null ? dto.localDescricao().trim() : null)
                .categoria(categoria)
                .tipoServico(tipoServico)
                .descricao(dto.descricao().trim())
                .estado(EstadoChamado.ABERTO)
                .build();

        chamado = chamadoRepository.save(chamado);

        // Registro Append-Only na Timeline
        HistoricoChamado historico = HistoricoChamado.builder()
                .chamado(chamado)
                .evento(EventoHistoricoChamado.CRIACAO)
                .estadoAnterior(null)
                .estadoNovo(EstadoChamado.ABERTO)
                .ator(solicitante)
                .observacao("Abertura do chamado pelo solicitante")
                .build();
        historicoRepository.save(historico);

        // REGISTRO NO FLUXO DE AUDITORIA GERAL (PGM)
        String localExibicao = (sala != null) ? sala.getNumeroIdentificacao() : dto.localDescricao().trim();
        AuditContextHolder.setFriendlyId(chamado.getProtocolo());
        AuditContextHolder.setLogDetalhes(String.format(
                "ABERTURA: Chamado aberto com protocolo %s. Demanda: '%s' - '%s'. Local: %s (%s). Solicitante: %s.",
                chamado.getProtocolo(),
                categoria.getNome(),
                tipoServico.getNome(),
                chamado.getAndar().getRotulo(),
                localExibicao,
                solicitante.getName()
        ));

        return buscarDetalhes(chamado.getId());
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResumoResponseDTO> listarMeusChamados(Pageable pageable) {
        Usuario solicitante = getUsuarioAutenticado();
        return chamadoRepository.findBySolicitanteId(solicitante.getId(), pageable)
                .map(this::toResumoDTO);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResumoResponseDTO> listarFilaGeral(
            EstadoChamado estado,
            Integer categoriaId,
            Integer tipoServicoId,
            AndarCodigo andar,
            Integer salaId,
            Integer responsavelId,
            Pageable pageable
    ) {
//        validarPermissao("MANUTENCAO_FILA_GERAL");

        Specification<ChamadoManutencao> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (estado != null) predicates.add(cb.equal(root.get("estado"), estado));
            if (categoriaId != null) predicates.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            if (tipoServicoId != null) predicates.add(cb.equal(root.get("tipoServico").get("id"), tipoServicoId));
            if (andar != null) predicates.add(cb.equal(root.get("andar"), andar));
            if (salaId != null) predicates.add(cb.equal(root.get("sala").get("id"), salaId));
            if (responsavelId != null) predicates.add(cb.equal(root.get("responsavel").get("id"), responsavelId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return chamadoRepository.findAll(spec, pageable).map(this::toResumoDTO);
    }

    @Transactional(readOnly = true)
    public ChamadoDetalheResponseDTO buscarDetalhes(Integer id) {
        ChamadoManutencao chamado = chamadoRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado de manutenção não encontrado: " + id));

        List<HistoricoChamadoResponseDTO> historico = historicoRepository.findByChamadoIdOrderByDataHoraAsc(id)
                .stream()
                .map(this::toHistoricoDTO)
                .toList();

        return toDetalheDTO(chamado, historico);
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entity = "Chamado de Manutenção")
    public ChamadoDetalheResponseDTO atribuirResponsavel(Integer id, AtribuicaoChamadoRequestDTO dto) {
        // validarPermissao("MANUTENCAO_ATENDER");

        Usuario ator = getUsuarioAutenticado();

        ChamadoManutencao chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado: " + id));

        if (chamado.getEstado().isFinalizado()) {
            throw new BusinessException("Não é possível atribuir um chamado já finalizado (" + chamado.getEstado().getRotulo() + ").");
        }

        Usuario novoResponsavel = usuarioRepository.findById(dto.responsavelId())
                .orElseThrow(() -> new ResourceNotFoundException("Responsável informado não encontrado."));

        if (!novoResponsavel.isEnabled()) {
            throw new BusinessException("O responsável selecionado está desativado no sistema.");
        }

        // Validação: Somente servidores com perfil 'almoxarifado' podem receber atribuição
        boolean isAlmoxarifado = novoResponsavel.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase(SecurityRoles.ALMOXARIFADO));

        if (!isAlmoxarifado) {
            throw new BusinessException("O usuário selecionado não possui a role 'almoxarifado' para receber atribuição.");
        }

        Usuario responsavelAnterior = chamado.getResponsavel();
        EstadoChamado estadoAnterior = chamado.getEstado();

        boolean isReatribuicao = (estadoAnterior != EstadoChamado.ABERTO && responsavelAnterior != null);

        EventoHistoricoChamado evento = (estadoAnterior == EstadoChamado.ABERTO)
                ? EventoHistoricoChamado.ATRIBUICAO
                : EventoHistoricoChamado.REATRIBUICAO;

        chamado.setEstado(EstadoChamado.ATRIBUIDO);
        chamado.setResponsavel(novoResponsavel);

        if (chamado.getAtribuidoEm() == null) {
            chamado.setAtribuidoEm(LocalDateTime.now());
        }

        chamadoRepository.save(chamado);

        HistoricoChamado hist = HistoricoChamado.builder()
                .chamado(chamado)
                .evento(evento)
                .estadoAnterior(estadoAnterior)
                .estadoNovo(EstadoChamado.ATRIBUIDO)
                .responsavelAnterior(responsavelAnterior)
                .responsavelNovo(novoResponsavel)
                .ator(ator)
                .observacao(dto.observacao())
                .build();
        historicoRepository.save(hist);

        // REGISTRO NO FLUXO DE AUDITORIA GERAL (PGM)
        String obsAuditoria = (dto.observacao() != null && !dto.observacao().isBlank())
                ? dto.observacao().trim()
                : "Sem observação";

        AuditContextHolder.setFriendlyId(chamado.getProtocolo());
        if (isReatribuicao) {
            AuditContextHolder.setLogDetalhes(String.format(
                    "REATRIBUIÇÃO: Chamado %s reatribuído de '%s' para '%s' pelo operador '%s'. Observação: %s.",
                    chamado.getProtocolo(),
                    responsavelAnterior.getName(),
                    novoResponsavel.getName(),
                    ator.getName(),
                    obsAuditoria
            ));
        } else {
            AuditContextHolder.setLogDetalhes(String.format(
                    "ATRIBUIÇÃO: Chamado %s atribuído ao responsável '%s' pelo operador '%s'. Observação: %s.",
                    chamado.getProtocolo(),
                    novoResponsavel.getName(),
                    ator.getName(),
                    obsAuditoria
            ));
        }

        return buscarDetalhes(id);
    }

    @Transactional(readOnly = true)
    public List<ResponsavelComboResponseDTO> listarResponsaveisDisponiveis() {
        return usuarioRepository.findUsuariosAptosAtribuicaoManutencao();
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entity = "Chamado de Manutenção")
    public ChamadoDetalheResponseDTO iniciarAtendimento(Integer id, IniciarAtendimentoRequestDTO dto) {
//        validarPermissao("MANUTENCAO_ATENDER");
        Usuario ator = getUsuarioAutenticado();

        ChamadoManutencao chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado: " + id));

        if (!chamado.getEstado().podeTransicionarPara(EstadoChamado.EM_ATENDIMENTO)) {
            throw new BusinessException("Transição não permitida: O chamado está " + chamado.getEstado().getRotulo() + " e não pode ser iniciado.");
        }

        chamado.setEstado(EstadoChamado.EM_ATENDIMENTO);
        chamado.setIniciadoEm(LocalDateTime.now());
        chamadoRepository.save(chamado);

        HistoricoChamado hist = HistoricoChamado.builder()
                .chamado(chamado)
                .evento(EventoHistoricoChamado.INICIO_ATENDIMENTO)
                .estadoAnterior(EstadoChamado.ATRIBUIDO)
                .estadoNovo(EstadoChamado.EM_ATENDIMENTO)
                .responsavelAnterior(chamado.getResponsavel())
                .responsavelNovo(chamado.getResponsavel())
                .ator(ator)
                .observacao(dto != null ? dto.observacao() : null)
                .build();
        historicoRepository.save(hist);

        // REGISTRO NO FLUXO DE AUDITORIA GERAL (PGM)
        String obsInicio = (dto != null && dto.observacao() != null && !dto.observacao().isBlank())
                ? dto.observacao().trim()
                : "Sem observação";
        AuditContextHolder.setFriendlyId(chamado.getProtocolo());
        AuditContextHolder.setLogDetalhes(String.format(
                "INÍCIO DE ATENDIMENTO: Execução dos serviços iniciada no chamado %s pelo operador '%s'. Observações iniciais: %s.",
                chamado.getProtocolo(),
                ator.getName(),
                obsInicio
        ));


        return buscarDetalhes(id);
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entity = "Chamado de Manutenção")
    public ChamadoDetalheResponseDTO concluirAtendimento(Integer id, ConcluirChamadoRequestDTO dto) {
//        validarPermissao("MANUTENCAO_ATENDER");

        Usuario ator = getUsuarioAutenticado();

        ChamadoManutencao chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado: " + id));

        if (!chamado.getEstado().podeTransicionarPara(EstadoChamado.CONCLUIDO)) {
            throw new BusinessException("Transição não permitida: Apenas chamados em atendimento podem ser concluídos.");
        }

        chamado.setEstado(EstadoChamado.CONCLUIDO);
        chamado.setConcluidoEm(LocalDateTime.now());
        chamadoRepository.save(chamado);

        HistoricoChamado hist = HistoricoChamado.builder()
                .chamado(chamado)
                .evento(EventoHistoricoChamado.CONCLUSAO)
                .estadoAnterior(EstadoChamado.EM_ATENDIMENTO)
                .estadoNovo(EstadoChamado.CONCLUIDO)
                .responsavelAnterior(chamado.getResponsavel())
                .responsavelNovo(chamado.getResponsavel())
                .ator(ator)
                .observacao(dto != null ? dto.observacao() : null)
                .build();
        historicoRepository.save(hist);

        // REGISTRO NO FLUXO DE AUDITORIA GERAL (PGM)
        String laudoConclusao = (dto != null && dto.observacao() != null && !dto.observacao().isBlank())
                ? dto.observacao().trim()
                : "Sem laudo informado";
        AuditContextHolder.setFriendlyId(chamado.getProtocolo());
        AuditContextHolder.setLogDetalhes(String.format(
                "CONCLUSÃO: Serviços concluídos no chamado %s pelo operador '%s'. Laudo/Observação: %s.",
                chamado.getProtocolo(),
                ator.getName(),
                laudoConclusao
        ));

        return buscarDetalhes(id);
    }

    @Transactional
    @Auditable(action = AuditAction.UPDATE, entity = "Chamado de Manutenção")
    public ChamadoDetalheResponseDTO cancelarChamado(Integer id, CancelarChamadoRequestDTO dto) {

        Usuario autenticado = getUsuarioAutenticado();

        ChamadoManutencao chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado: " + id));

//        if (!chamado.getSolicitante().getId().equals(solicitante.getId())) {
//            throw new ForbiddenException("Apenas o próprio solicitante pode cancelar o seu chamado.");
//        }

        if (chamado.getEstado().isFinalizado()) {
            throw new BusinessException("Não é possível cancelar um chamado que já se encontra " + chamado.getEstado().getRotulo() + ".");
        }

        boolean isSolicitante = chamado.getSolicitante().getId().equals(autenticado.getId());

        boolean isTramitador = autenticado.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase(SecurityRoles.TRAMITADOR));

        // Regra de Negócio de Cancelamento:
        // - Se ABERTO: Solicitante ou Tramitador podem cancelar.
        // - Se DIFERENTE DE ABERTO: SOMENTE Tramitador pode cancelar.
        if (chamado.getEstado() == EstadoChamado.ABERTO) {

            if (!isSolicitante && !isTramitador) {
                throw new ForbiddenException("Apenas o próprio solicitante ou um usuário tramitador pode cancelar um chamado aberto.");
            }
        } else {

            if (!isTramitador) {
                throw new ForbiddenException("Apenas usuários com a role 'tramitador' têm permissão para cancelar chamados em andamento.");
            }
        }

        String papelAtor = isSolicitante ? "Solicitante" : "Tramitador";

        EstadoChamado estadoAnt = chamado.getEstado();
        chamado.setEstado(EstadoChamado.CANCELADO);
        chamado.setCanceladoEm(LocalDateTime.now());
        chamado.setMotivoCancelamento(dto.motivo().trim());
        chamadoRepository.save(chamado);

        HistoricoChamado hist = HistoricoChamado.builder()
                .chamado(chamado)
                .evento(EventoHistoricoChamado.CANCELAMENTO)
                .estadoAnterior(estadoAnt)
                .estadoNovo(EstadoChamado.CANCELADO)
                .ator(autenticado)
                .observacao("Motivo: " + dto.motivo().trim())
                .build();
        historicoRepository.save(hist);

        // REGISTRO NO FLUXO DE AUDITORIA GERAL (PGM)
        AuditContextHolder.setFriendlyId(chamado.getProtocolo());
        AuditContextHolder.setLogDetalhes(String.format(
                "CANCELAMENTO: Chamado %s cancelado no estado '%s' por '%s' (%s). Motivo: %s.",
                chamado.getProtocolo(),
                estadoAnt.getRotulo(),
                autenticado.getName(),
                papelAtor,
                dto.motivo().trim()
        ));

        return buscarDetalhes(id);
    }

    // --- MÉTODOS AUXILIARES ---

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            throw new ForbiddenException("Usuário não autenticado no contexto de segurança.");
        }
        return (Usuario) auth.getPrincipal();
    }

    private void validarPermissao(String authority) {
        Usuario user = getUsuarioAutenticado();
        boolean hasAuth = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase(authority) || a.getAuthority().equalsIgnoreCase("admin"));
        if (!hasAuth) {
            throw new ForbiddenException("Acesso negado: Requer autoridade " + authority);
        }
    }

    private synchronized String gerarProtocoloUnico() {
        LocalDateTime agora = LocalDateTime.now();
        String dataBase = agora.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long sequencialDia = chamadoRepository.countChamadosCriadosApos(LocalDate.now().atStartOfDay()) + 1;
        return String.format("MAN-%s-%04d", dataBase, sequencialDia);
    }

    private ChamadoResumoResponseDTO toResumoDTO(ChamadoManutencao c) {
        String local = (c.getSala() != null) ? c.getSala().getNumeroIdentificacao() : c.getLocalDescricao();
        String solicitanteNome = c.getSolicitante().getName();
        String responsavelNome = (c.getResponsavel() != null) ? c.getResponsavel().getName() : "Não atribuído";

        return new ChamadoResumoResponseDTO(
                c.getId(),
                c.getProtocolo(),
                c.getAndar(),
                c.getAndar().getRotulo(),
                local,
                c.getSala() != null ? c.getSala().getId() : null,
                c.getCategoria().getNome(),
                c.getTipoServico().getNome(),
                c.getEstado(),
                c.getEstado().getRotulo(),
                solicitanteNome,
                responsavelNome,
                c.getCriadoEm(),
                c.getAtribuidoEm(),
                c.getConcluidoEm()
        );
    }

    private ChamadoDetalheResponseDTO toDetalheDTO(ChamadoManutencao c, List<HistoricoChamadoResponseDTO> historico) {
        return new ChamadoDetalheResponseDTO(
                c.getId(),
                c.getProtocolo(),
                c.getAndar(),
                c.getAndar().getRotulo(),
                c.getSala() != null ? c.getSala().getId() : null,
                c.getSala() != null ? c.getSala().getNumeroIdentificacao() : null,
                c.getLocalDescricao(),
                c.getCategoria().getId(),
                c.getCategoria().getNome(),
                c.getTipoServico().getId(),
                c.getTipoServico().getNome(),
                c.getDescricao(),
                c.getEstado(),
                c.getEstado().getRotulo(),
                c.getSolicitante().getId(),
                c.getSolicitante().getName(),
                c.getResponsavel() != null ? c.getResponsavel().getId() : null,
                c.getResponsavel() != null ? c.getResponsavel().getName() : null,
                c.getCriadoEm(),
                c.getAtribuidoEm(),
                c.getIniciadoEm(),
                c.getConcluidoEm(),
                c.getCanceladoEm(),
                c.getMotivoCancelamento(),
                historico
        );
    }

    private HistoricoChamadoResponseDTO toHistoricoDTO(HistoricoChamado h) {
        return new HistoricoChamadoResponseDTO(
                h.getId(),
                h.getEvento(),
                h.getEvento().getDescricao(),
                h.getEstadoAnterior(),
                h.getEstadoNovo(),
                h.getResponsavelAnterior() != null ? h.getResponsavelAnterior().getName() : null,
                h.getResponsavelNovo() != null ? h.getResponsavelNovo().getName() : null,
                h.getAtor().getName(),
                h.getDataHora(),
                h.getObservacao()
        );
    }
}
