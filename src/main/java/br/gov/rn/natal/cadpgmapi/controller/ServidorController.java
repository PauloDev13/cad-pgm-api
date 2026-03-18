package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorResponseDTO;
import br.gov.rn.natal.cadpgmapi.service.ServidorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/servidores")
@RequiredArgsConstructor
@Tag(name = "Servidores", description = "API de Gestão de Servidores")
public class ServidorController {
    private final ServidorService service;

    @PostMapping
    @Operation(summary = "Cadastrar novo servidor")
    public ResponseEntity<ServidorResponseDTO> create(@Valid @RequestBody ServidorRequestDTO dto) {
        ServidorResponseDTO novoServidor = service.create(dto);
        // Monta a URL dinâmica: http://localhost:8080/api/v1/cargoes/5
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(novoServidor.id()).toUri();

        return ResponseEntity.created(location).body(service.create(dto));
    }

    @GetMapping
    @Operation(summary = "Listar servidores com paginação")
    public Page<ServidorResponseDTO> findAll(
            @ParameterObject
            @PageableDefault(sort = "nome", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return service.findAll(pageable);
    }

    @GetMapping("/busca")
    @Operation(summary = "Buscar servidor por CPF ou Matrícula",
            description = "Informe o CPF ou a Matrícula via query parameter. Exemplo: /busca?cpf=00011122233")
    public ServidorResponseDTO findByCpfOrMatricula(
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String matricula) {

        return service.findByCpfOrMatricula(cpf, matricula);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar servidor por ID")
    public ServidorResponseDTO findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar servidor existente")
    public ServidorResponseDTO update(
            @PathVariable Integer id,
            @RequestBody @Valid ServidorRequestDTO dto
    ) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover servidor")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
