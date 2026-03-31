package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.dto.request.AliasRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.AliasResponseDTO;
import br.gov.rn.natal.cadpgmapi.service.AliasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/alias")
@RequiredArgsConstructor
@Tag(name = "Alias", description = "API de gestão de alias para email")
public class AliasController {
    private final AliasService aliasService;

    @PostMapping
    @Operation(summary = "Cadastrar novo Alias")
    public ResponseEntity<AliasResponseDTO> create(@Valid @RequestBody AliasRequestDTO dto) {
        AliasResponseDTO novoAlias = aliasService.create(dto);
        // Monta a URL dinâmica: http://localhost:8080/api/v1/aliases/5
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(novoAlias.id()).toUri();

        return ResponseEntity.created(location).body(novoAlias);
    }

    @GetMapping
    @Operation(summary = "Listar os Alias com paginação")
    public Page<AliasResponseDTO> findAll(Pageable pageable) {
        return aliasService.findAll(pageable);
    }

    @GetMapping("/select")
    @Operation(summary = "Listar os Alias sem paginação")
    public List<AliasResponseDTO> findAllSelect() {
        return aliasService.findAllSelect();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Alias por ID")
    public AliasResponseDTO findById(@PathVariable Integer id) {
        return aliasService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Alias existente")
    public AliasResponseDTO update(
            @PathVariable Integer id,
            @Valid @RequestBody AliasRequestDTO dto) {
        return aliasService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover Alias")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        aliasService.delete(id);
    }
}
