package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.dto.request.SetorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SetorResponseDTO;
import br.gov.rn.natal.cadpgmapi.service.SetorService;
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
@RequestMapping("/api/v1/setores")
@RequiredArgsConstructor
@Tag(name = "Setores", description = "API de gestão de setores")
public class SetorController {
    private final SetorService setorService;

    @PostMapping
    @Operation(summary = "Cadastrar novo setor")
    public ResponseEntity<SetorResponseDTO> create(@Valid @RequestBody SetorRequestDTO dto) {
        SetorResponseDTO novoSetor = setorService.create(dto);
        // Monta a URL dinâmica: http://localhost:8080/api/v1/setores/5
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(novoSetor.id()).toUri();

        return ResponseEntity.created(location).body(novoSetor);
    }

    @GetMapping
    @Operation(summary = "Listar todos os Setores")
    public List<SetorResponseDTO> findAll() {
        return setorService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Setor por ID")
    public SetorResponseDTO findById(@PathVariable Integer id) {
        return setorService.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Setor existente")
    public SetorResponseDTO update(
            @PathVariable Integer id,
            @Valid @RequestBody SetorRequestDTO dto) {
        return setorService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover Setor")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        setorService.delete(id);
    }
}
