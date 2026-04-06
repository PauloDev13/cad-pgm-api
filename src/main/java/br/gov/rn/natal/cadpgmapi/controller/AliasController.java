package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.controller.generic.BaseController;
import br.gov.rn.natal.cadpgmapi.dto.request.AliasRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.AliasResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Alias;
import br.gov.rn.natal.cadpgmapi.service.AliasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alias")
@Tag(name = "Alias", description = "API de gestão de alias para email")
public class AliasController extends BaseController<Alias, AliasRequestDTO, AliasResponseDTO, Integer> {
    private final AliasService aliasService;

    // Construtor
    public AliasController(AliasService service) {
        super(service);
        this.aliasService = service;
    }
    // Implementação obrigatória do méthod abstrato do pai!
    @Override
    protected Integer getIdFromDto(AliasResponseDTO dto) {
        return dto.id();
    }
    @Override
    protected String getDefaultSortProperty() {
        return "email";
    }

    // O ENDPOINT DE FILTRO CUSTOMIZADO SÓ DELE
    @GetMapping("/searchFilter")
    @Operation(summary = "Buscar Alias por email com paginação")
    public Page<AliasResponseDTO> findByEmail(
            @RequestParam(required = false) String email,
            @ParameterObject Pageable pageable) {

        return aliasService.findByFilterEmail(email, pageable);
    }
}
