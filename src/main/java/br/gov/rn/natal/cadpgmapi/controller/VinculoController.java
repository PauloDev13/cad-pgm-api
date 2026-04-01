package br.gov.rn.natal.cadpgmapi.controller;


import br.gov.rn.natal.cadpgmapi.controller.generic.BaseNameController;
import br.gov.rn.natal.cadpgmapi.dto.request.VinculoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.VinculoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Vinculo;
import br.gov.rn.natal.cadpgmapi.service.VinculoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
;

@RestController
@RequestMapping("/api/v1/vinculos")
@Tag(name = "Vínculos", description = "API de gestão de vínculos")
public class VinculoController extends BaseNameController<Vinculo, VinculoRequestDTO, VinculoResponseDTO, Integer> {
    // Construtor
    public VinculoController(VinculoService service) {
        super(service);
    }
    // Implementação obrigatória do méthod abstrato do pai!
    @Override
    protected Integer getIdFromDto(VinculoResponseDTO dto) {
        return dto.id();
    }
    @Override
    protected String getDefaultSortProperty() {
        return "nome";
    }
}
