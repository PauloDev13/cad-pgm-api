package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorResponseDTO;
import br.gov.rn.natal.cadpgmapi.service.ServidorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/servidores")
@RequiredArgsConstructor
@Tag(name = "Servidores", description = "API de Gestão de Servidores")

public class ServidorController {
    private final ServidorService service;

    @PostMapping
    @Operation(summary = "Cadastrar novo servidor")
    public ResponseEntity<ServidorResponseDTO> create(@RequestBody @Valid ServidorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @GetMapping
    @Operation(summary = "Listar servidores com paginação")
    public ResponseEntity<Page<ServidorResponseDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar servidor por ID")
    public ResponseEntity<ServidorResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar servidor existente")
    public ResponseEntity<ServidorResponseDTO> update(
            @PathVariable Integer id,
            @RequestBody @Valid ServidorRequestDTO dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover servidor")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
