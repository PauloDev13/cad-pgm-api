package br.gov.rn.natal.cadpgmapi.manutencao.controller;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.AndarCodigo;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.EstadoChamado;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.*;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.ChamadoDetalheResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.ChamadoResumoResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.ResponsavelComboResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.service.ChamadoManutencaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manutencao/chamados")
@RequiredArgsConstructor
@Tag(name = "Manutenção - Chamados", description = "Operações de ciclo de vida e consulta de chamados de manutenção")
public class ChamadoManutencaoController {

    private final ChamadoManutencaoService chamadoService;

    // 1. Abertura: Exige estritamente a role 'manutencao'

    @PostMapping
    @PreAuthorize("hasAuthority('manutencao')")
    @Operation(summary = "Abre um novo chamado de manutenção", description = "Solicitante obtido do token JWT. Resposta 201 Created.")
    public ResponseEntity<ChamadoDetalheResponseDTO> abrirChamado(@Valid @RequestBody AberturaChamadoRequestDTO dto) {
        ChamadoDetalheResponseDTO response = chamadoService.abrirChamado(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/meus")
    @Operation(summary = "Lista os chamados do usuário solicitante autenticado", description = "Paginação e ordenação padrão decrescente por criadoEm.")
    public ResponseEntity<Page<ChamadoResumoResponseDTO>> listarMeusChamados(
            @ParameterObject @PageableDefault(sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(chamadoService.listarMeusChamados(pageable));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('almoxarifado') and hasAuthority('tramitador')")
    @Operation(summary = "Consulta a fila geral de chamados", description = "Acesso restrito à equipe autorizada do almoxarifado e administradores.")
    public ResponseEntity<Page<ChamadoResumoResponseDTO>> listarFilaGeral(
            @RequestParam(required = false) EstadoChamado estado,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer tipoServicoId,
            @RequestParam(required = false) AndarCodigo andar,
            @RequestParam(required = false) Integer salaId,
            @RequestParam(required = false) Integer responsavelId,
            @ParameterObject @PageableDefault(sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(chamadoService.listarFilaGeral(
                estado, categoriaId, tipoServicoId, andar, salaId, responsavelId, pageable)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Recupera o detalhe do chamado com sua timeline completa de histórico")
    public ResponseEntity<ChamadoDetalheResponseDTO> buscarDetalhes(@PathVariable Integer id) {
        return ResponseEntity.ok(chamadoService.buscarDetalhes(id));
    }

    // 2. Dropdown de Responsáveis: Apenas 'almoxarifado' E 'tramitador' podem visualizar a lista para atribuir
    @GetMapping("/responsaveis-disponiveis")
    @PreAuthorize("hasAuthority('almoxarifado') and hasAuthority('tramitador')")
    @Operation(summary = "Lista servidores do almoxarifado aptos para receber atribuição de chamados")
    public ResponseEntity<List<ResponsavelComboResponseDTO>> listarResponsaveisDisponiveis() {
        return ResponseEntity.ok(chamadoService.listarResponsaveisDisponiveis());
    }

    // 3. Atribuição: Requer roles 'almoxarifado' E 'tramitador'
    @PostMapping("/{id}/atribuicao")
    @PreAuthorize("hasAuthority('almoxarifado') and hasAuthority('tramitador')")
    @Operation(summary = "Atribui ou reatribui o chamado a um servidor do almoxarifado",
            description = "Requer autoridade MANUTENCAO_ATENDER.")
    public ResponseEntity<ChamadoDetalheResponseDTO> atribuirResponsavel(
            @PathVariable Integer id,
            @Valid @RequestBody AtribuicaoChamadoRequestDTO dto
    ) {
        return ResponseEntity.ok(chamadoService.atribuirResponsavel(id, dto));
    }

    // 4. Iniciar: Requer roles 'almoxarifado' E 'tramitador'
    @PostMapping("/{id}/iniciar")
    @PreAuthorize("hasAuthority('almoxarifado') and hasAuthority('tramitador')")
    @Operation(summary = "Inicia o atendimento operacional do chamado",
            description = "Transiciona de ATRIBUIDO para EM_ATENDIMENTO.")
    public ResponseEntity<ChamadoDetalheResponseDTO> iniciarAtendimento(
            @PathVariable Integer id,
            @RequestBody(required = false) IniciarAtendimentoRequestDTO dto
    ) {
        return ResponseEntity.ok(chamadoService.iniciarAtendimento(id, dto));
    }

    // 5. Concluir: Requer roles 'almoxarifado' E 'tramitador'
    @PostMapping("/{id}/concluir")
    @PreAuthorize("hasAuthority('almoxarifado') and hasAuthority('tramitador')")
    @Operation(summary = "Conclui a execução do chamado",
            description = "Transiciona de EM_ATENDIMENTO para CONCLUIDO.")
    public ResponseEntity<ChamadoDetalheResponseDTO> concluirAtendimento(
            @PathVariable Integer id,
            @RequestBody(required = false) ConcluirChamadoRequestDTO dto
    ) {
        return ResponseEntity.ok(chamadoService.concluirAtendimento(id, dto));
    }

    // 6. Cancelamento: Acesso autenticado na borda; validação fina (Solicitante vs Tramitador) no Service
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancela o chamado",
            description = "Apenas o solicitante pode executar, enquanto o chamado não estiver finalizado.")
    public ResponseEntity<ChamadoDetalheResponseDTO> cancelarChamado(
            @PathVariable Integer id,
            @Valid @RequestBody CancelarChamadoRequestDTO dto
    ) {
        return ResponseEntity.ok(chamadoService.cancelarChamado(id, dto));
    }
}