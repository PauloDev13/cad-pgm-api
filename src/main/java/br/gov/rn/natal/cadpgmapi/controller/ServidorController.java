package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.controller.generic.BaseController;
import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.service.ServidorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/servidores")
@Tag(name = "Servidores", description = "API de Gestão de Servidores")
public class ServidorController extends BaseController<
        Servidor, ServidorRequestDTO, ServidorResponseDTO, Integer> {

    private final ServidorService service;

    public ServidorController(ServidorService service) {
        super(service);
        this.service = service;
    }

    // Ensina ao pai como extrair o ID para montar a URL do HTTP 201
    @Override
    protected Integer getIdFromDto(ServidorResponseDTO dto) {
        return dto.id();
    }

    //  Define que, se o usuário não mandar paginação, a lista vem ordenada por nome!
    @Override
    protected String getDefaultSortProperty() {
        return "nome";
    }

    // Mantemos APENAS o endpoint que é exclusivo desta entidade
    @GetMapping("/searchFilter")
    @Operation(summary = "Buscar servidor por CPF, Matrícula, Nome e Status",
            description = "Informe o CPF ou a Matrícula ou o Status via query parameter. " +
                    "Exemplo: /searchFilter?cpf=00011122233")
    public Page<ServidorResponseDTO> findByFilters(
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer statusId,
            @ParameterObject @PageableDefault(
                    sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        return service.findByFilters(cpf, matricula, nome, statusId, pageable);
    }

    // Endpoint para a aba de excluídos. Lista todos os registros
    @GetMapping("/excluded")
    @Operation(summary = "Buscar todos os servidores com status de excluído",
            description = "Retorna os registros excluídos com 'Soft Delete'")
    public Page<ServidorResponseDTO> getExcluded(
            @ParameterObject @PageableDefault(
                    sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.listExcluded(pageable);
    }

    // Busca um servidor excluído pelo ID
    @GetMapping("/excluded/{id}")
    @Operation(summary = "Busca um servidor com status de excluído",
            description = "Retorna um registro excluído com 'Soft Delete'")
    public ServidorResponseDTO getExcludedById( @PathVariable Integer id) {
        return service.getExcludedById(id);
    }

    @GetMapping("/searchExcluded")
    @Operation(summary = "Buscar por Nome ou CPF servidores com status excluído",
            description = "Informe o Nome ou o CPF via query parameter. " +
                    "Exemplo: /searchExcluded?cpf=00011122233")
    public Page<ServidorResponseDTO> searchExcluded(
            @RequestParam(required = false) String term,
            @ParameterObject @PageableDefault(
                    sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        return service.searchExcluded(term, pageable);
    }

    // Endpoint que o botão do Modal vai chamar para alterar o status de excluído
    // Usamos PATCH pois é uma alteração parcial/específica
    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Reativa o cadastro que está com status excluído",
            description = "Inverte o fluxo do 'Soft Delete'")
    public ResponseEntity<ServidorResponseDTO> reactivate(
            @PathVariable Integer id,
            @RequestBody ServidorRequestDTO dto) {
        return ResponseEntity.ok(service.reativated(id, dto));
    }
}
