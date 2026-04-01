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
                    "Exemplo: /searchFilter?cpf=00011122233&matricula=T0001")
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
}
