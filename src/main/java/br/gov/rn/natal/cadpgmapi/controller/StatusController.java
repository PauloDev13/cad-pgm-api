package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.dto.request.StatusRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.StatusResponseDTO;
import br.gov.rn.natal.cadpgmapi.service.StatusService;
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
@RequestMapping("/api/v1/status")
@RequiredArgsConstructor
@Tag(name = "Status", description = "API de gestão de Status")
public class StatusController {
    private final StatusService statusService;

    @PostMapping
    @Operation(summary = "Cadastrar novo Status")
    public ResponseEntity<StatusResponseDTO> create(@Valid @RequestBody StatusRequestDTO dto) {
        StatusResponseDTO novoStatus = statusService.create(dto);
        // Monta a URL dinâmica: http://localhost:8080/api/v1/statuss/5
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(novoStatus.id()).toUri();

        return ResponseEntity.created(location).body(novoStatus);
    }

    @GetMapping
    @Operation(summary = "Listar todos os Status")
    public List<StatusResponseDTO> findAll() {
        return statusService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Status por ID")
    public StatusResponseDTO findById(@PathVariable Integer id) {
        return statusService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Status existente")
    public StatusResponseDTO update(
            @PathVariable Integer id,
            @Valid @RequestBody StatusRequestDTO dto) {
        return statusService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover Status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        statusService.delete(id);
    }
}
