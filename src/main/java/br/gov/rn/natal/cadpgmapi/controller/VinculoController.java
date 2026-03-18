package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.dto.request.VinculoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.VinculoResponseDTO;
import br.gov.rn.natal.cadpgmapi.service.VinculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vinculos")
@RequiredArgsConstructor
@Tag(name = "Vínculos", description = "API de gestão de vínculos")
public class VinculoController {
    private final VinculoService vinculoService;

    @PostMapping
    @Operation(summary = "Cadastrar novo Vínculo")
    public ResponseEntity<VinculoResponseDTO> create(@Valid @RequestBody VinculoRequestDTO dto) {
        VinculoResponseDTO novoVinculo = vinculoService.create(dto);
        // Monta a URL dinâmica: http://localhost:8080/api/v1/vinculoes/5
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(novoVinculo.id()).toUri();

        return ResponseEntity.created(location).body(novoVinculo);
    }

    @GetMapping
    @Operation(summary = "Listar todos os Vínculos")
    public List<VinculoResponseDTO> findAll() {
        return vinculoService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Vínculo por ID")
    public VinculoResponseDTO findById(@PathVariable Integer id) {
        return vinculoService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Vínculo existente")
    public VinculoResponseDTO update(
            @PathVariable Integer id,
            @Valid @RequestBody VinculoRequestDTO dto) {
        return vinculoService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover Vínculo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        vinculoService.delete(id);
    }
}
