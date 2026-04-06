package br.gov.rn.natal.cadpgmapi.auth.controller;

import br.gov.rn.natal.cadpgmapi.auth.dto.LoginRequestDTO;
import br.gov.rn.natal.cadpgmapi.auth.dto.LoginResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para login e emissão de tokens de acesso")
public class AuthController {
    @PostMapping("/login")
    @Operation(
            summary = "Realizar login no sistema",
            description = "Recebe as credenciais do usuário e " +
                    "devolve um token JWT para acesso às rotas protegidas. (Em fase de implementação)"
    )
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {

        // TODO: Fase 2 - Chamar o AuthenticationManager do Spring Security para validar login/senha no banco
        // TODO: Fase 2 - Chamar o TokenService para gerar o JWT real com base no usuário autenticado

        // Retorno "Mockado" (Falso) apenas para o frontend já ir testando a integração
        // e o Swagger renderizar a documentação corretamente.
        LoginResponseDTO mockResponse = new LoginResponseDTO(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock-token-temporario-123456",
                "Bearer"
        );

        return ResponseEntity.ok(mockResponse);
    }
}
