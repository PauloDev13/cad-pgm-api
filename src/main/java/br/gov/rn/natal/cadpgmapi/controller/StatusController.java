package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.controller.generic.BaseController;
import br.gov.rn.natal.cadpgmapi.dto.request.StatusRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.StatusResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Status;
import br.gov.rn.natal.cadpgmapi.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/status")
@Tag(name = "Status", description = "API de gestão de Status")
public class StatusController extends BaseController<Status, StatusRequestDTO, StatusResponseDTO, Integer> {
    private final StatusService statusService;

    // Construtor
    public StatusController(StatusService service, StatusService statusService) {
        super(service);
        this.statusService = statusService;
    }
    // Implementação obrigatória do méthod abstrato do pai!
    @Override
    protected Integer getIdFromDto(StatusResponseDTO dto) {
        return dto.id();
    }
    @Override
    protected String getDefaultSortProperty() {
        return "descricao";
    }

    // O ENDPOINT DE FILTRO CUSTOMIZADO SÓ DELE
    @GetMapping("/searchFilter")
    @Operation(summary = "Buscar Status por nome com paginação")
    public Page<StatusResponseDTO> findByEmail(
            @RequestParam(required = false) String descricao,
            @ParameterObject Pageable pageable) {

        return statusService.findByFilterDescricao(descricao, pageable);
    }
}
