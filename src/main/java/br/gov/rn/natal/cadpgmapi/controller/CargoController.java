package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.dto.request.CargoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.CargoResponseDTO;
import br.gov.rn.natal.cadpgmapi.service.CargoService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cargos")
@RequiredArgsConstructor
@Tag(name = "Cargos", description = "API de gestão de cargos")
public class CargoController {
    private final CargoService cargoService;

    @PostMapping
    @Operation(summary = "Cadastrar novo cargo")
    public ResponseEntity<CargoResponseDTO> create(@Valid @RequestBody CargoRequestDTO dto) {
        CargoResponseDTO novoCargo = cargoService.create(dto);
        // Monta a URL dinâmica: http://localhost:8080/api/v1/cargoes/5
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(novoCargo.id()).toUri();

        return ResponseEntity.created(location).body(novoCargo);
    }

    @GetMapping
    @Operation(summary = "Listar Cargos com paginação")
    public Page<CargoResponseDTO> findAll(
            @ParameterObject
            @PageableDefault(sort = "nome", direction = Sort.Direction.ASC)Pageable pageable) {
        return cargoService.findAll(pageable);
    }

    @GetMapping("select")
    @Operation(summary = "Listar Cargos sem paginação")
    public List<CargoResponseDTO> findAllSelect() {
        return cargoService.findAllSelect();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Cargo por ID")
    public CargoResponseDTO findById(@PathVariable Integer id) {
        return cargoService.findById(id);
    }

    @GetMapping("/searchFilter")
    @Operation(summary = "Buscar cargos por nome com paginação")
    public ResponseEntity<Page<CargoResponseDTO>> findByNome(
            @RequestParam(required = false) String nome,
            @ParameterObject @PageableDefault(
                    sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(cargoService.findByFilterName(nome, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Cargo existente")
    public CargoResponseDTO update(
            @PathVariable Integer id,
            @Valid @RequestBody CargoRequestDTO dto) {
        return cargoService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover Cargo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        cargoService.delete(id);
    }
}
