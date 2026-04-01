package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.controller.generic.BaseNameController;
import br.gov.rn.natal.cadpgmapi.dto.request.ProcuradorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Procurador;
import br.gov.rn.natal.cadpgmapi.service.ProcuradorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/procuradores")
@Tag(name = "Procuradores", description = "API de gestão de procuradores")
public class ProcuradorController extends BaseNameController<
        Procurador, ProcuradorRequestDTO, ProcuradorResponseDTO, Integer> {

    // Construtor
    public ProcuradorController(ProcuradorService service) {
        super(service);

    }
    // Implementação obrigatória do méthod abstrato do pai!
    @Override
    protected Integer getIdFromDto(ProcuradorResponseDTO dto) {
        return dto.id();
    }
    @Override
    protected String getDefaultSortProperty() {
        return "nome";
    }
}
