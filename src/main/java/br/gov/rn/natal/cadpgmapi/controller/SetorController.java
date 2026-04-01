package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.controller.generic.BaseNameController;
import br.gov.rn.natal.cadpgmapi.dto.request.SetorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SetorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Setor;
import br.gov.rn.natal.cadpgmapi.service.SetorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/setores")
@Tag(name = "Setores", description = "API de gestão de setores")
public class SetorController extends BaseNameController<Setor, SetorRequestDTO, SetorResponseDTO, Integer> {
    // Construtor
    public SetorController(SetorService service) {
        super(service);

    }

    // Implementação obrigatória do méthod abstrato do pai!
    @Override
    protected Integer getIdFromDto(SetorResponseDTO dto) {
        return dto.id();
    }

    @Override
    protected String getDefaultSortProperty() {
        return "nome";
    }
}
