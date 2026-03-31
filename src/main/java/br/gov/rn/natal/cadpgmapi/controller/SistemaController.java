package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.dto.request.SistemaRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SistemaResponseDTO;
import br.gov.rn.natal.cadpgmapi.service.SistemaService;
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
@RequestMapping("/api/v1/sistemas")
@RequiredArgsConstructor
@Tag(name = "Sistemas", description = "API de gestão de sistemas")
public class SistemaController {
    private final SistemaService sistemaService;

    @PostMapping
    @Operation(summary = "Cadastrar novo Sistema")
    public ResponseEntity<SistemaResponseDTO> create(@Valid @RequestBody SistemaRequestDTO dto) {
        SistemaResponseDTO novoSistema = sistemaService.create(dto);
        // Monta a URL dinâmica: http://localhost:8080/api/v1/sistemas/5
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(novoSistema.id()).toUri();

        return ResponseEntity.created(location).body(novoSistema);
    }

    @GetMapping
    @Operation(summary = "Listar todos os Sistema")
    public Page<SistemaResponseDTO> findAll(Pageable pageable) {
        return sistemaService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Sistema por ID")
    public SistemaResponseDTO findById(@PathVariable Integer id) {
        return sistemaService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Sistema existente")
    public SistemaResponseDTO update(
            @PathVariable Integer id,
            @Valid @RequestBody SistemaRequestDTO dto) {
        return sistemaService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover Sistema")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        sistemaService.delete(id);
    }
}
