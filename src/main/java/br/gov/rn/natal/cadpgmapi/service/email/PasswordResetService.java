package br.gov.rn.natal.cadpgmapi.service.email;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
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
    @Auditable(action = AuditAction.PASSWORD_RECOVERY, entity = "Segurança")
    public void requestEmailReconvery(String email) {
        Usuario user = usuarioRepository.findByEmail(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "E-mail não cadastrado. Verifique se digitou corretamente"));

        // 2. Alimenta os detalhes da auditoria antes do envio
        AuditContextHolder.setEntityName("Usuário");
        AuditContextHolder.setFriendlyId(user.getUsername());
        AuditContextHolder.setLogDetalhes("Link para recuperação de senha enviada para o e-mail: " + user.getEmail());

        // Delega a geração para o TokenService (Stateless)
        String token = tokenService.generatePasswordRecoveryToken(user);

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
        emailService.sendEmailRecovery(user.getEmail(), frontendUrl);
    }

    @Transactional
    @Auditable(action = AuditAction.PASSWORD_RESET, entity = "Segurança")
    public void resetPassword(String token, String newPassword) {
        // 1. Reutiliza a lógica de validação que acabamos de criar
        Usuario user = validateToken(token);

        AuditContextHolder.setEntityName("Usuário");
        AuditContextHolder.setFriendlyId(user.getUsername());
        AuditContextHolder.setLogDetalhes("Senha redefinida com sucesso através de link de recuperação.");

        // 2. Atualiza a senha corretamente
        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        usuarioRepository.save(user);
    }

    // Verifica se o token é válido - se não está expirado ou já foi usado para redefinir a senha
    public Usuario validateToken(String token) {
        // Valida a assinatura e a expiração do JWT
        DecodedJWT jwt = tokenService.validateRecoveryToken(token);

        String email = jwt.getSubject();
        String oldHash = jwt.getClaim("hash").asString();

        // Busca o usuário pelo e-mail contido no token
        Usuario user = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        // VALIDAÇÃO DE REUSO
        if (!user.getPassword().equals(oldHash)) {
            throw new BusinessException("Este link de recuperação já foi utilizado ou é inválido.");
        }

        return user; // Retorna o usuário validado
    }
}
