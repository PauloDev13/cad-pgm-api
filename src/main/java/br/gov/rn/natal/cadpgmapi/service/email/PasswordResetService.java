package br.gov.rn.natal.cadpgmapi.service.email;

import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.entity.email.PasswordResetToken;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import br.gov.rn.natal.cadpgmapi.repository.email.PasswordResetTokenRepository;
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
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void solicitarRecuperacao(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email.trim());

        // Defesa contra Enumeration Attack: Se o e-mail não existir, finalizamos o méthod em silêncio.
        if (usuarioOpt.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioOpt.get();

        // 1. Gera o Token Único
        String token = UUID.randomUUID().toString();

        // 2. Salva no banco com validade de 30 minutos
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .usuario(usuario)
                .dataExpiracao(LocalDateTime.now().plusMinutes(30))
                .build();
        tokenRepository.save(resetToken);

        // 3. Monta o link do Frontend e dispara o e-mail
        String frontendUrl = "http://localhost:4200/auth/redefinir-senha?token=" + token;
        emailService.enviarEmailRecuperacao(usuario.getEmail(), frontendUrl);
    }

    @Transactional
    public void redefinirSenha(String token, String newPassword) {
        // 1. Busca o Token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Token inválido ou inexistente."));

        // 2. Valida se já foi usado
        if (resetToken.isUsado()) {
            throw new BusinessException("Este link de recuperação já foi utilizado.");
        }

        // 3. Valida se está expirado
        if (resetToken.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Este link de recuperação expirou. Solicite um novo.");
        }

        // 4. Atualiza a senha do usuário
        Usuario usuario = resetToken.getUsuario();

        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setPassword(newPassword);
        usuarioRepository.save(usuario);

        // 5. Invalida o token para não ser usado novamente
        resetToken.setUsado(true);
        tokenRepository.save(resetToken);
    }
}
