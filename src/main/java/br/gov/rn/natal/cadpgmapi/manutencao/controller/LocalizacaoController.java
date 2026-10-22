package br.gov.rn.natal.cadpgmapi.manutencao.controller;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.AndarCodigo;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.AndarResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.SalaResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.service.LocalizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manutencao")
@RequiredArgsConstructor
@Tag(name = "Manutenção - Localização", description = "Endpoints para consulta de andares e salas da PGM")
public class LocalizacaoController {

    private final LocalizacaoService localizacaoService;

    @GetMapping("/andares")
    @Operation(summary = "Lista os andares disponíveis no prédio", description = "Retorna os valores fixos do enum AndarCodigo")
    public ResponseEntity<List<AndarResponseDTO>> listarAndares() {
        return ResponseEntity.ok(localizacaoService.listarAndares());
    }

    @GetMapping("/salas")
    @Operation(summary = "Lista as salas ativas filtradas por andar", description = "Retorna as salas ativas vinculadas ao andar informado")
    public ResponseEntity<List<SalaResponseDTO>> listarSalasPorAndar(@RequestParam AndarCodigo andar) {
        return ResponseEntity.ok(localizacaoService.listarSalasPorAndar(andar));
    }
}
