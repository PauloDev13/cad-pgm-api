package br.gov.rn.natal.cadpgmapi.manutencao.controller;

import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.CategoriaDemandaRequestDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.TipoServicoRequestDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.CategoriaDemandaResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.TipoServicoResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.service.CatalogoManutencaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manutencao")
@RequiredArgsConstructor
@Tag(name = "Manutenção - Catálogo", description = "Gerenciamento e consulta do catálogo de serviços de manutenção")
public class CatalogoManutencaoController {

    private final CatalogoManutencaoService catalogoService;

    /*
     LISTA TODAS AS CATEGORIAS (ATIVA E INATIVAS)
     */
    @GetMapping("/categorias")
    @Operation(summary = "Lista categorias (ativas e inativas) de demanda")
    public ResponseEntity<List<CategoriaDemandaResponseDTO>> listarCategorias() {
        return ResponseEntity.ok(catalogoService.listarCategorias());
    }

    /*
     LISTA TODAS AS CATEGORIAS ATIVAS
     */
    @GetMapping("/categorias-ativas")
    @Operation(summary = "Lista categorias ativas de demanda")
    public ResponseEntity<List<CategoriaDemandaResponseDTO>> listarCategoriasAtivas() {
        return ResponseEntity.ok(catalogoService.listarCategoriasAtivas());
    }

    /*
    LISTA TODOS OS TIPOS DE SERVIÇOS (ATIVOS E INATIVOS) POR CATEGORIA
   */
    @GetMapping("/categorias/{id}/tipos-servico")
    @Operation(summary = "Lista os tipos de serviço (ativos e inativos) para a categoria informada")
    public ResponseEntity<List<TipoServicoResponseDTO>> listarTiposPorCategoria(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoService.listarTiposPorCategoria(id));
    }

    /*
    LISTA TOS TIPOS DE SERVIÇOS ATIVOS POR CATEGORIA
    */
    @GetMapping("/categorias/{id}/tipos-servico-ativos")
    @Operation(summary = "Lista os tipos de serviço ativos para a categoria informada")
    public ResponseEntity<List<TipoServicoResponseDTO>> listarTiposAtivosPorCategoria(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoService.listarTiposAtivosPorCategoria(id));
    }

    @PostMapping("/admin/categorias")
    @Operation(summary = "Cadastra uma nova categoria de demanda (Administrador)")
    public ResponseEntity<CategoriaDemandaResponseDTO> criarCategoria(@Valid @RequestBody CategoriaDemandaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoService.criarCategoria(dto));
    }

    @PutMapping("/admin/categorias/{id}")
    @Operation(summary = "Atualiza uma categoria existente (Administrador)")
    public ResponseEntity<CategoriaDemandaResponseDTO> atualizarCategoria(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaDemandaRequestDTO dto
    ) {
        return ResponseEntity.ok(catalogoService.atualizarCategoria(id, dto));
    }

    @PostMapping("/admin/tipos-servico")
    @Operation(summary = "Cadastra um novo tipo de serviço (Administrador)")
    public ResponseEntity<TipoServicoResponseDTO> criarTipoServico(@Valid @RequestBody TipoServicoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoService.criarTipoServico(dto));
    }

    @PutMapping("/admin/tipos-servico/{id}")
    @Operation(summary = "Atualiza um tipo de serviço (Administrador)")
    public ResponseEntity<TipoServicoResponseDTO> atualizarTipoServico(
            @PathVariable Integer id,
            @Valid @RequestBody TipoServicoRequestDTO dto
    ) {
        return ResponseEntity.ok(catalogoService.atualizarTipoServico(id, dto));
    }

    @PatchMapping("/admin/tipos-servico/{id}/status")
    @Operation(summary = "Ativa ou desativa um tipo de serviço (Administrador)")
    public ResponseEntity<Void> alterarStatusTipoServico(@PathVariable Integer id, @RequestParam boolean ativo) {
        catalogoService.alterarStatusTipoServico(id, ativo);
        return ResponseEntity.noContent().build();
    }
}
