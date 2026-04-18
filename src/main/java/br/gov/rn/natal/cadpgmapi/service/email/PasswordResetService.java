package br.gov.rn.natal.cadpgmapi.service.email;

import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import br.gov.rn.natal.cadpgmapi.security.TokenService;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestEmailReconvery(String email) {
        Usuario user = usuarioRepository.findByEmail(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "E-mail não cadastrado. Verifique se digitou corretamente"));

//        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email.trim());

        // Defesa contra Enumeration Attack: Se o e-mail não existir, finalizamos o méthod em silêncio.
//        if (usuarioOpt.isEmpty()) {
//            return;
//        }
//        Usuario usuario = usuarioOpt.get();

        // Delega a geração para o TokenService (Stateless)
        String token = tokenService.gerarTokenRecuperacaoSenha(user);

        // Monta o link do Frontend e dispara o e-mail
        String frontendUrl = "http://localhost:4200/auth/redefinir-senha?token=" + token;
        emailService.enviarEmailRecuperacao(user.getEmail(), frontendUrl);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        // 1. Reutiliza a lógica de validação que acabamos de criar
        Usuario usuario = validateToken(token);

        // 2. Atualiza a senha corretamente
        usuario.setPassword(passwordEncoder.encode(newPassword.trim()));
        usuarioRepository.save(usuario);
    }

    // Verifica se o token é válido - se não está expirado ou já foi usado para redefinir a senha
    public Usuario validateToken(String token) {
        // Valida a assinatura e a expiração do JWT
        DecodedJWT jwt = tokenService.validarTokenRecuperacao(token);

        String email = jwt.getSubject();
        String hashAntigo = jwt.getClaim("hash").asString();

        // Busca o usuário pelo e-mail contido no token
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        // VALIDAÇÃO DE REUSO
        if (!usuario.getPassword().equals(hashAntigo)) {
            throw new BusinessException("Este link de recuperação já foi utilizado ou é inválido.");
        }

        return usuario; // Retorna o usuário validado
    }
}
