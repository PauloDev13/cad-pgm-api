package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.controller.generic.BaseNameController;
import br.gov.rn.natal.cadpgmapi.dto.request.CargoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.CargoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Cargo;
import br.gov.rn.natal.cadpgmapi.service.CargoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cargos")
@Tag(name = "Cargos", description = "API de gestão de cargos")
public class CargoController extends BaseNameController<Cargo, CargoRequestDTO, CargoResponseDTO, Integer> {
    // Construtor
    public CargoController(CargoService service) {
        super(service);

    }
    // Implementação obrigatória do méthod abstrato do pai!
    @Override
    protected Integer getIdFromDto(CargoResponseDTO dto) {
        return dto.id();
    }
    @Override
    protected String getDefaultSortProperty() {
        return "nome";
    }
}
