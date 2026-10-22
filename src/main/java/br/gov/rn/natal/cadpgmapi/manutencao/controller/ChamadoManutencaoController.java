package br.gov.rn.natal.cadpgmapi.manutencao.controller;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.AndarCodigo;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.EstadoChamado;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.*;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.ChamadoDetalheResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.ChamadoResumoResponseDTO;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/manutencao/chamados")
@RequiredArgsConstructor
@Tag(name = "Manutenção - Chamados", description = "Operações de ciclo de vida e consulta de chamados de manutenção")
public class ChamadoManutencaoController {

    private final ChamadoManutencaoService chamadoService;

    @PostMapping
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
    @Operation(summary = "Consulta a fila geral de chamados", description = "Acesso restrito à equipe autorizada do almoxarifado e administradores.")
    public ResponseEntity<Page<ChamadoResumoResponseDTO>> listarFilaGeral(
            @RequestParam(required = false) EstadoChamado estado,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Integer tipoServicoId,
            @RequestParam(required = false) AndarCodigo andar,
            @RequestParam(required = false) Integer salaId,
            @RequestParam(required = false) Integer responsavelId,
            @ParameterObject @PageableDefault(sort = "criadoEm", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(chamadoService.listarFilaGeral(estado, categoriaId, tipoServicoId, andar, salaId, responsavelId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recupera o detalhe do chamado com sua timeline completa de histórico")
    public ResponseEntity<ChamadoDetalheResponseDTO> buscarDetalhes(@PathVariable Integer id) {
        return ResponseEntity.ok(chamadoService.buscarDetalhes(id));
    }

    @PostMapping("/{id}/atribuicao")
    @Operation(summary = "Atribui ou reatribui o chamado a um servidor do almoxarifado", description = "Requer autoridade MANUTENCAO_ATENDER.")
    public ResponseEntity<ChamadoDetalheResponseDTO> atribuirResponsavel(
            @PathVariable Integer id,
            @Valid @RequestBody AtribuicaoChamadoRequestDTO dto
    ) {
        return ResponseEntity.ok(chamadoService.atribuirResponsavel(id, dto));
    }

    @PostMapping("/{id}/iniciar")
    @Operation(summary = "Inicia o atendimento operacional do chamado",
            description = "Transiciona de ATRIBUIDO para EM_ATENDIMENTO.")
    public ResponseEntity<ChamadoDetalheResponseDTO> iniciarAtendimento(
            @PathVariable Integer id,
            @RequestBody(required = false) IniciarAtendimentoRequestDTO dto
    ) {
        return ResponseEntity.ok(chamadoService.iniciarAtendimento(id, dto));
    }

    @PostMapping("/{id}/concluir")
    @Operation(summary = "Conclui a execução do chamado",
            description = "Transiciona de EM_ATENDIMENTO para CONCLUIDO.")
    public ResponseEntity<ChamadoDetalheResponseDTO> concluirAtendimento(
            @PathVariable Integer id,
            @RequestBody(required = false) ConcluirChamadoRequestDTO dto
    ) {
        return ResponseEntity.ok(chamadoService.concluirAtendimento(id, dto));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancela o chamado",
            description = "Apenas o solicitante pode executar, enquanto o chamado não estiver finalizado.")
    public ResponseEntity<ChamadoDetalheResponseDTO> cancelarChamado(
            @PathVariable Integer id,
            @Valid @RequestBody CancelarChamadoRequestDTO dto
    ) {
        return ResponseEntity.ok(chamadoService.cancelarChamado(id, dto));
    }
}