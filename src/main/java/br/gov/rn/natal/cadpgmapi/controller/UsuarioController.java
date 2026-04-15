package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.auth.dto.response.AdminResetPasswordResponseDTO;
import br.gov.rn.natal.cadpgmapi.controller.generic.BaseController;
import br.gov.rn.natal.cadpgmapi.dto.request.UsuarioRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.UsuarioResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.update.UsuarioUpdateDTO;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários", description = "API de gestão de Usuários do sistema")
public class UsuarioController extends BaseController<Usuario, UsuarioRequestDTO, UsuarioResponseDTO, Integer> {
    private final UsuarioService usuarioService;

    // Construtor
    public UsuarioController(UsuarioService service) {
        super(service);
        this.usuarioService = service;
    }
    // Implementação obrigatória do méthod abstrato do pai!
    @Override
    protected Integer getIdFromDto(UsuarioResponseDTO dto) {
        return dto.id();
    }
    @Override
    protected String getDefaultSortProperty() {
        return "userName";
    }

    // O ENDPOINT DE FILTRO CUSTOMIZADO SÓ DELE
    @GetMapping("/searchFilter")
    @Operation(summary = "Buscar Usuário por nome, login e email com paginação")
    public Page<UsuarioResponseDTO> findByFilters(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String email,
            @ParameterObject @PageableDefault(
                    sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        return usuarioService.findByFilters(name, userName, email, pageable);
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Admin: Resetar senha de um usuário",
            description = "Gera uma senha aleatória e força troca no próximo login."
    )
    public ResponseEntity<AdminResetPasswordResponseDTO> resetSenhaAdmin(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.resetPasswordByAdmin(id));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar Perfil do Usuário",
            description = "Atualiza os dados cadastrais do usuário sem modificar a senha.")
    public ResponseEntity<UsuarioResponseDTO> updateProfile(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioUpdateDTO dto) {

        return ResponseEntity.ok(usuarioService.updateProfile(id, dto));
    }
}
