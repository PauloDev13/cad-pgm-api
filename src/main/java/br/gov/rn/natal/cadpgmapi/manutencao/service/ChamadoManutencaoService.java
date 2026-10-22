package br.gov.rn.natal.cadpgmapi.manutencao.service;

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
        validarPermissao("MANUTENCAO_FILA_GERAL");

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

        Usuario autenticado = getUsuarioAutenticado();
        boolean isOwner = chamado.getSolicitante().getId().equals(autenticado.getId());
        boolean hasQueueAccess = autenticado.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("MANUTENCAO_FILA_GERAL") || a.getAuthority().equals("admin"));

        if (!isOwner && !hasQueueAccess) {
            throw new ForbiddenException("Você não tem autorização para visualizar este chamado.");
        }

        List<HistoricoChamadoResponseDTO> historico = historicoRepository.findByChamadoIdOrderByDataHoraAsc(id)
                .stream()
                .map(this::toHistoricoDTO)
                .toList();

        return toDetalheDTO(chamado, historico);
    }

    @Transactional
    public ChamadoDetalheResponseDTO atribuirResponsavel(Integer id, AtribuicaoChamadoRequestDTO dto) {
        validarPermissao("MANUTENCAO_ATENDER");
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

        Usuario responsavelAnterior = chamado.getResponsavel();
        EstadoChamado estadoAnterior = chamado.getEstado();
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

        return buscarDetalhes(id);
    }

    @Transactional
    public ChamadoDetalheResponseDTO iniciarAtendimento(Integer id, IniciarAtendimentoRequestDTO dto) {
        validarPermissao("MANUTENCAO_ATENDER");
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

        return buscarDetalhes(id);
    }

    @Transactional
    public ChamadoDetalheResponseDTO concluirAtendimento(Integer id, ConcluirChamadoRequestDTO dto) {
        validarPermissao("MANUTENCAO_ATENDER");
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

        return buscarDetalhes(id);
    }

    @Transactional
    public ChamadoDetalheResponseDTO cancelarChamado(Integer id, CancelarChamadoRequestDTO dto) {
        Usuario solicitante = getUsuarioAutenticado();

        ChamadoManutencao chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado: " + id));

        if (!chamado.getSolicitante().getId().equals(solicitante.getId())) {
            throw new ForbiddenException("Apenas o próprio solicitante pode cancelar o seu chamado.");
        }

        if (chamado.getEstado().isFinalizado()) {
            throw new BusinessException("Não é possível cancelar um chamado que já se encontra " + chamado.getEstado().getRotulo() + ".");
        }

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
                .ator(solicitante)
                .observacao("Motivo: " + dto.motivo().trim())
                .build();
        historicoRepository.save(hist);

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
