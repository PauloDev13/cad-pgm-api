package br.gov.rn.natal.cadpgmapi.auth.controller;

import br.gov.rn.natal.cadpgmapi.auth.dto.request.ForceChangePasswordRequestDTO;
import br.gov.rn.natal.cadpgmapi.auth.dto.request.LoginRequestDTO;
import br.gov.rn.natal.cadpgmapi.auth.dto.response.LoginResponseDTO;
import br.gov.rn.natal.cadpgmapi.auth.service.AuthService;
import br.gov.rn.natal.cadpgmapi.dto.request.email.ForgotPasswordRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.email.ResetPasswordRequestDTO;
import br.gov.rn.natal.cadpgmapi.service.email.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para login e emissão de tokens de acesso")
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    @Operation(summary = "Realizar login no sistema",
            description = "Recebe as credenciais do usuário e " +
                    "devolve um token JWT para acesso às rotas protegidas. (Em fase de implementação)"
    )
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {

        // Chama o serviço passando o login e senha digitados
        String tokenJWT = authService.authenticate(dto);

        return ResponseEntity.ok(new LoginResponseDTO(tokenJWT));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperação de senha",
            description = "Envia um link de redefinição para o e-mail informado (se existir). Retorna sempre 200 OK.")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO dto) {

        passwordResetService.solicitarRecuperacao(dto.email());

        // Retorno genérico de sucesso (independente de ter achado no banco ou não)
        return ResponseEntity.ok("Se o e-mail existir em nossa base, um link de recuperação foi enviado.");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Efetivar redefinição de senha",
            description = "Valida o token enviado pelo e-mail e atualiza a senha do usuário.")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO dto) {

        passwordResetService.redefinirSenha(dto.token(), dto.newPassword());

        return ResponseEntity.ok("Senha redefinida com sucesso. Você já pode fazer login.");
    }

    @PostMapping("/force-password-change")
    @Operation(
            summary = "Usuário: Troca obrigatória de senha",
            description = "Finaliza o fluxo de troca de senha no primeiro acesso."
    )
    public ResponseEntity<Void> forcarTrocaSenha(@Valid @RequestBody ForceChangePasswordRequestDTO dto) {
        authService.finalizeRequiredPasswordChange(dto);
        return ResponseEntity.noContent().build();
    }
}
