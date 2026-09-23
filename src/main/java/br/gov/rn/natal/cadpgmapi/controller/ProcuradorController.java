package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.controller.generic.BaseNameController;
import br.gov.rn.natal.cadpgmapi.dto.request.ProcuradorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorSelectDTO;
import br.gov.rn.natal.cadpgmapi.entity.Procurador;
import br.gov.rn.natal.cadpgmapi.service.ProcuradorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/procuradores")
@Tag(name = "Procuradores", description = "API de gestão de procuradores")
public class ProcuradorController extends BaseNameController<
        Procurador, ProcuradorRequestDTO, ProcuradorResponseDTO, Integer> {

    private final ProcuradorService procuradorService;

    // Construtor
    public ProcuradorController(ProcuradorService service, ProcuradorService procuradorService) {
        super(service);
        this.procuradorService = procuradorService;
    }
    // Implementação obrigatória do método abstrato do pai!
    @Override
    protected Integer getIdFromDto(ProcuradorResponseDTO dto) {
        return dto.id();
    }
    @Override
    protected String getDefaultSortProperty() {
        return "nome";
    }


    @GetMapping("/select-combo")
    @Operation(summary = "Buscar lista de procuradores resumida",
            description = "Retorna lista de procuradores com ID e Nome para preenchimento de combos")
    public List<ProcuradorSelectDTO> getProcuradoresSelect() {
        return procuradorService.listarProcuradoresSelect();
    }
}
