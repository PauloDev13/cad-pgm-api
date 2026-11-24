package br.gov.rn.natal.cadpgmapi.manutencao.controller;

import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.RequisicoesEcidadeRequestDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.RequisicaoEcidadeResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.service.RequisicaoEcidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manutencao/chamados/{chamadoId}/requisicoes-ecidade")
@RequiredArgsConstructor
@Tag(name = "Manutenção - Requisições e-Cidade", description = "Operações para consulta e vinculação de requisições do e-Cidade aos chamados")
public class RequisicaoEcidadeController {
    private final RequisicaoEcidadeService requisicaoService;
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista as requisições do e-Cidade vinculadas a um chamado específico")
    public ResponseEntity<List<RequisicaoEcidadeResponseDTO>> listarPorChamado(@PathVariable Integer chamadoId) {
        return ResponseEntity.ok(requisicaoService.listarPorChamado(chamadoId));
    }
    @PostMapping
    @PreAuthorize("hasAuthority('almoxarifado') and hasAuthority('tramitador')")
    @Operation(summary = "Adiciona múltiplos números de requisição e-Cidade ao chamado")
    public ResponseEntity<List<RequisicaoEcidadeResponseDTO>> adicionarRequisicoes(
            @PathVariable Integer chamadoId,
            @Valid @RequestBody RequisicoesEcidadeRequestDTO dto
    ) {
        List<RequisicaoEcidadeResponseDTO> response = requisicaoService.vincularRequisicoes(
                chamadoId, dto.numerosRequisicao()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
