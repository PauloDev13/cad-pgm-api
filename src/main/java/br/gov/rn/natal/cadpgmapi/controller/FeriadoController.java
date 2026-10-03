package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.controller.generic.BaseNameController;
import br.gov.rn.natal.cadpgmapi.dto.request.FeriadoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.FeriadoResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.update.FeriadoStatusPatchDTO;
import br.gov.rn.natal.cadpgmapi.entity.Feriado;
import br.gov.rn.natal.cadpgmapi.service.FeriadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/feriados")
@Tag(name = "Feriados", description = "API de gestão de feriados e pontos facultativos para a Folha de Ponto")
public class FeriadoController extends BaseNameController<Feriado, FeriadoRequestDTO, FeriadoResponseDTO, Integer> {
    private final FeriadoService feriadoService;

    public FeriadoController(FeriadoService service) {
        super(service);
        this.feriadoService = service;
    }

    @Override
    protected Integer getIdFromDto(FeriadoResponseDTO dto) {
        return dto.id();
    }

    @Override
    protected String getDefaultSortProperty() {
        return "data";
    }

    @GetMapping("/ativos")
    @Operation(
            summary = "Buscar feriados ativos para a folha de ponto",
            description = "Retorna feriados com ativo=true, opcionalmente filtrados por mês"
    )
    public ResponseEntity<List<FeriadoResponseDTO>> getAtivos(
            @RequestParam(required = false) Integer mes) {
        return ResponseEntity.ok(feriadoService.findAtivos(mes));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Alternar status do checkbox de inclusão na folha de ponto",
            description = "Atualiza o campo ativo do feriado"
    )
    public ResponseEntity<FeriadoResponseDTO> toggleStatus(
            @PathVariable Integer id,
            @Valid @RequestBody FeriadoStatusPatchDTO dto) {
        return ResponseEntity.ok(feriadoService.toggleStatus(id, dto.ativo()));
    }
}
