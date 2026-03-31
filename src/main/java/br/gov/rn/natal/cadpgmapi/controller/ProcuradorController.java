package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.dto.request.ProcuradorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.service.ProcuradorService;
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
@RequestMapping("/api/v1/procuradores")
@RequiredArgsConstructor
@Tag(name = "Procuradores", description = "API de gestão de procuradores")
public class ProcuradorController {
    private final ProcuradorService procuradorService;

    @PostMapping
    @Operation(summary = "Cadastrar novo procurador")
    public ResponseEntity<ProcuradorResponseDTO> create(@Valid @RequestBody ProcuradorRequestDTO dto) {
        ProcuradorResponseDTO novoProcurador = procuradorService.create(dto);
        // Monta a URL dinâmica: http://localhost:8080/api/v1/procuradores/5
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(novoProcurador.id()).toUri();

        return ResponseEntity.created(location).body(novoProcurador);
    }

    @GetMapping("/select")
    @Operation(summary = "Listar os Procuradores sem paginação")
    public List<ProcuradorResponseDTO> findAllSelect() {
        return procuradorService.findAllSelect();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Procurador por ID")
    public ProcuradorResponseDTO findById(@PathVariable Integer id) {
        return procuradorService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Procurador existente")
    public ProcuradorResponseDTO update(
            @PathVariable Integer id,
            @Valid @RequestBody ProcuradorRequestDTO dto) {
        return procuradorService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover Procurador")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        procuradorService.delete(id);
    }
}
