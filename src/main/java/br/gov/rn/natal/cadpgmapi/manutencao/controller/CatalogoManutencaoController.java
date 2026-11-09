package br.gov.rn.natal.cadpgmapi.manutencao.controller;

import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.CategoriaDemandaRequestDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.SubcategoriaRequestDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.TipoServicoRequestDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.CategoriaDemandaResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.SubcategoriaResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.TipoServicoResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.service.CatalogoManutencaoService;
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
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manutencao")
@RequiredArgsConstructor
@Tag(name = "Manutenção - Catálogo", description = "Gerenciamento e consulta do catálogo de serviços de manutenção")
public class CatalogoManutencaoController {

    private final CatalogoManutencaoService catalogoService;

                // ==============================
                //          TABELAS
                // ==============================

    /*
     * LISTA TODAS AS CATEGORIAS (ATIVAS/INATIVAS) COM PAGINAÇÃO (ADMINISTRATIVO)
     */
    @GetMapping("/categorias/paginadas")
    @Operation(summary = "Lista CATEGORIAS (ativas e inativas) com paginação")
    public ResponseEntity<Page<CategoriaDemandaResponseDTO>> listarCategoriasPaginadas(
            @ParameterObject
            @PageableDefault(sort = {"ordemExibicao", "nome"}, direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(catalogoService.listarCategoriasPaginada(pageable));
    }

    /*
     * LISTA TODAS AS SUBCATEGORIAS (ATIVAS/INATIVAS OU FILTRADAS) COM PAGINAÇÃO (ADMINISTRATIVO)
     */
    @GetMapping("/subcategorias/paginadas")
    @Operation(
            summary = "Lista as SUBCATEGORIAS (ativas e inativas) com paginação e filtros " +
                    "opcionais por categoria e ativo")
    public ResponseEntity<Page<SubcategoriaResponseDTO>> listarSubcategoriasPaginadas(
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) Boolean ativo,
            @ParameterObject
            @PageableDefault(sort = {"ordemExibicao", "nome"}, direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(catalogoService.listarSubcategoriasPaginada(categoriaId, ativo,pageable));
    }

    /*
    LISTA TODOS OS TIPOS DE SERVIÇOS (ATIVOS E INATIVOS) COM PAGINAÇÃO (ADMINISTRATIVO)
   */
    @GetMapping("/tipos-servico/paginados")
    @Operation(summary = "Lista os TIPOS DE SERVIÇOS (ativos e inativos) com paginação e filtros " +
            "opcionais por categoria e ativo\"")
    public ResponseEntity<Page<TipoServicoResponseDTO>> listarTiposServicosPaginada(
            @RequestParam(required = false) Integer subcategoriaId,
            @RequestParam(required = false) Boolean ativo,
            @ParameterObject
            @PageableDefault(sort = {"ordemExibicao", "nome"}, direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(catalogoService.listarTiposServicosPaginada(subcategoriaId, ativo, pageable));
    }

                // ==============================
                //          DROPDOWNS
                // ==============================

    /*
     LISTA TODAS AS CATEGORIAS ATIVAS (DROPDOWN EM CASCATA - NÍVEL 1)
     */
    @GetMapping("/categorias-ativas")
    @Operation(summary = "Lista CATEGORIAS ativas")
    public ResponseEntity<List<CategoriaDemandaResponseDTO>> listarCategoriasAtivas() {
        return ResponseEntity.ok(catalogoService.listarCategoriasAtivas());
    }

    /*
     * LISTA TODAS AS SUBCATEGORIAS ATIVAS (DROPDOWN EM CASCATA - NÍVEL 2)
     */
    @GetMapping("/subcategorias-ativas")
    @Operation(summary = "Lista as SUBCATEGORIAS ativas")
    public ResponseEntity<List<SubcategoriaResponseDTO>> listarSubcategoriasAtivas() {
        return ResponseEntity.ok(catalogoService.listarSubcategoriasAtivas());
    }

    /*
     * LISTA TODAS AS SUBCATEGORIAS ATIVAS POR CATEGORIA (DROPDOWN EM CASCATA - NÍVEL 2)
     */
    @GetMapping("/categorias/{id}/subcategorias-ativas")
    @Operation(summary = "Lista as SUBCATEGORIAS ativas para a uma categoria informada")
    public ResponseEntity<List<SubcategoriaResponseDTO>> listarSubcategoriasAtivasPorCategoria(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoService.listarSubcategoriasAtivasPorCategoria(id));
    }

    /*
    LISTA TOS TIPOS DE SERVIÇOS ATIVOS POR SUBCATEGORIA (DROPDOWN EM CASCATA - NÍVEL 3)
    */
    @GetMapping("/subcategorias/{id}/tipos-servico-ativos")
    @Operation(summary = "Lista os TIPOS DE SERVIÇOS ativos para uma subcategoria informada")
    public ResponseEntity<List<TipoServicoResponseDTO>> listarTiposAtivosPorSubcategoria(@PathVariable Integer id) {
        return ResponseEntity.ok(catalogoService.listarTiposAtivosPorSubcategoria(id));
    }

                    // ==============================
                    //              CREATE
                    // ==============================

    /*
    CRIAR UMA NOVA CATEGORIA  (ADMINISTRATIVO)
    */
    @PostMapping("/admin/categorias")
    @Operation(summary = "Cadastra uma NOVA CATEGORIA (Administrador)")
    public ResponseEntity<CategoriaDemandaResponseDTO> criarCategoria(@Valid @RequestBody CategoriaDemandaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoService.criarCategoria(dto));
    }

    /*
     * CRIA UMA NOVA SUBCATEGORIA (ADMINISTRATIVO)
     */
    @PostMapping("/admin/subcategorias")
    @Operation(summary = "Cadastra uma NOVA SUBCATEGORIA (Administrador)")
    public ResponseEntity<SubcategoriaResponseDTO> criarSubcategoria(
            @Valid @RequestBody SubcategoriaRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoService.criarSubcategoria(dto));
    }

    /*
    CRIA UM NOVO TIPO DE SERVIÇO  (ADMINISTRATIVO)
    */
    @PostMapping("/admin/tipos-servico")
    @Operation(summary = "Cadastra um NOVO TIPO DE SERVIÇO (Administrador)")
    public ResponseEntity<TipoServicoResponseDTO> criarTipoServico(@Valid @RequestBody TipoServicoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoService.criarTipoServico(dto));
    }

                // ==============================
                //              UPDATE
                // ==============================

    /*
    ATUALIZA UMA CATEGORIA
    */
    @PutMapping("/admin/categorias/{id}")
    @Operation(summary = "Atualiza uma CATEGORIA existente (Administrador)")
    public ResponseEntity<CategoriaDemandaResponseDTO> atualizarCategoria(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaDemandaRequestDTO dto
    ) {
        return ResponseEntity.ok(catalogoService.atualizarCategoria(id, dto));
    }

    /*
     * ATUALIZA UMA SUBCATEGORIA (ADMINISTRATIVO)
     */
    @PutMapping("/admin/subcategorias/{id}")
    @Operation(summary = "Atualiza uma SUBCATEGORIA existente (Administrador)")
    public ResponseEntity<SubcategoriaResponseDTO> atualizarSubcategoria(
            @PathVariable Integer id,
            @Valid @RequestBody SubcategoriaRequestDTO dto
    ) {
        return ResponseEntity.ok(catalogoService.atualizarSubcategoria(id, dto));
    }

    /*
    ATUALIZA UM TIPO DE SERVIÇO  (ADMINISTRATIVO)
    */
    @PutMapping("/admin/tipos-servico/{id}")
    @Operation(summary = "Atualiza um TIPO DE SERVIÇO existente (Administrador)")
    public ResponseEntity<TipoServicoResponseDTO> atualizarTipoServico(
            @PathVariable Integer id,
            @Valid @RequestBody TipoServicoRequestDTO dto
    ) {
        return ResponseEntity.ok(catalogoService.atualizarTipoServico(id, dto));
    }

    // =================================================
    //              UPDATE ADMINISTRATIVOS
    // =================================================

    /*
     * [NOVO ENDPOINT] ATUALIZA O STATUS (ATIVO/INATIVO) DE UMA CATEGORIA EM CASCATA (ADMINISTRATIVO)
     */
    @PatchMapping("/admin/categorias/{id}/status")
    @Operation(summary = "Ativa ou desativa uma CATEGORIA em cascata (Administrador)")
    public ResponseEntity<Void> alterarStatusCategoria(
            @PathVariable Integer id,
            @RequestParam boolean ativo
    ) {
        catalogoService.alterarStatusCategoria(id, ativo);
        return ResponseEntity.noContent().build();
    }
    /*
     * ATUALIZA O STATUS (ATIVO/INATIVO) DE UMA SUBCATEGORIA (ADMINISTRATIVO)
     */
    @PatchMapping("/admin/subcategorias/{id}/status")
    @Operation(summary = "Ativa ou desativa uma SUBCATEGORIA (Administrador)")
    public ResponseEntity<Void> alterarStatusSubcategoria(
            @PathVariable Integer id,
            @RequestParam boolean ativo
    ) {
        catalogoService.alterarStatusSubcategoria(id, ativo);
        return ResponseEntity.noContent().build();
    }
    /*
    ALTERAR O STATUS (ATIVO/INATIVO) DE UM TIPO DE SERVIÇO  (ADMINISTRATIVO)
    */
    @PatchMapping("/admin/tipos-servico/{id}/status")
    @Operation(summary = "Ativa ou desativa um TIPO DE SERVIÇO (Administrador)")
    public ResponseEntity<Void> alterarStatusTipoServico(@PathVariable Integer id, @RequestParam boolean ativo) {
        catalogoService.alterarStatusTipoServico(id, ativo);
        return ResponseEntity.noContent().build();
    }
}
