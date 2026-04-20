package br.gov.rn.natal.cadpgmapi.service.email;

import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import br.gov.rn.natal.cadpgmapi.security.TokenService;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

        // Delega a geração para o TokenService (Stateless)
        String token = tokenService.gerarTokenRecuperacaoSenha(user);

        // Descobre de onde o usuário está acessando agora
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();

        // Pega a origem exata de onde vem a requisição
        String originRequestUser = request.getHeader("Origin");

        // 2. Fallback de segurança (caso o cabeçalho venha nulo por algum motivo)
        if (originRequestUser == null || originRequestUser.isEmpty()) {
            originRequestUser = "http://localhost:4200";
        }

        // Monta o link do Frontend e dispara o e-mail
        String frontendUrl = originRequestUser + "/auth/redefinir-senha?token=" + token;
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
