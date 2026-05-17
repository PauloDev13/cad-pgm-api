package br.gov.rn.natal.cadpgmapi.auth.service;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.audit.services.AuditService;
import br.gov.rn.natal.cadpgmapi.auth.dto.request.ForceChangePasswordRequestDTO;
import br.gov.rn.natal.cadpgmapi.auth.dto.request.LoginRequestDTO;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ForbiddenException;
import br.gov.rn.natal.cadpgmapi.exception.UnauthorizedException;
import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import br.gov.rn.natal.cadpgmapi.security.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Auditable(action = AuditAction.LOGIN, entity = "Acesso ao Sistema")
    public String authenticate(LoginRequestDTO dto) {
        // Criamos um "envelope" padronizado do Spring com as credenciais cruas que o usuário digitou
        var credenciais = new UsernamePasswordAuthenticationToken(dto.userName(), dto.password());

        try{
            // Se o código chegou nesta linha, significa que o login e senha estão corretos!
            var authentication = authenticationManager.authenticate(credenciais);
            // Extraímos o nosso usuário de dentro do objeto de autenticação
            var authenticatedUser = (Usuario) authentication.getPrincipal();

            // 🌟 2. O PULO DO GATO: Injetamos a autenticação no contexto atual
            // Assim, a classe que salva a auditoria vai conseguir saber QUEM fez a ação
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 🌟 3. Preparamos o registro de auditoria
            AuditContextHolder.setEntityName("Autenticação");
            AuditContextHolder.setFriendlyId(authenticatedUser.getUsername()); // ou getLogin()
            AuditContextHolder.setLogDetalhes("Login realizado com sucesso.");


            // Pedimos para a nossa fábrica gerar a Pulseira VIP (JWT) para este usuário e a retornamos
            return tokenService.generateToken(authenticatedUser);

            // Exceção para senha ou login errados
        } catch (BadCredentialsException e) {
            // Registra as tentativas de login que falharam
            registerLoginFailure(dto.userName(), "Falha de Autenticação: Senha ou usuário inválidos.");
            throw new UnauthorizedException("<strong>Usuário</strong> ou <strong>Senha</strong> inválidos");
            // Exceção para usuário inativo
        } catch (DisabledException e) {
            registerLoginFailure(dto.userName(), "Tentativa de acesso em conta INATIVA.");
            throw new ForbiddenException("Conta <strong>INATIVA</strong>. Procure o Administrador");
        // Exceção para usuário bloqueado
        }catch (LockedException e) {
            registerLoginFailure(dto.userName(), "Tentativa de acesso em conta BLOQUEADA.");
            throw new ForbiddenException("Conta <strong>BLOQUEADA</strong>. Procure o Administrador");
            // Qualquer outra exceção
        } catch (AuthenticationException e) {
            registerLoginFailure(dto.userName(), "Falha desconhecida de autenticação: " + e.getMessage());
            throw new UnauthorizedException("Não foi possível autenticar: " + e.getMessage());
        }
    }

    // MÉTODOS PARA RESET DE SENHA DO USUÁRIO PELO ADMINISTRADOR
    @Transactional
    public void finalizeRequiredPasswordChange(ForceChangePasswordRequestDTO dto) {
        // Extraímos o login (userName) diretamente do Token JWT que está logado no momento!
        String userNameLogado = SecurityContextHolder.getContext().getAuthentication().getName();

        // Buscamos o usuário no banco usando a identidade do Token
        Usuario user = usuarioRepository.findByUserName(userNameLogado)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        // Buscamos o usuário no banco usando a identidade do Token
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        user.setForcePasswordChange(false); // Libera o acesso normal
        usuarioRepository.save(user);
    }

    // MÉTODOS PRIVADOS
    // 🌟 Método auxiliar para gravar a falha direto no banco, sem depender da anotação
    private void registerLoginFailure(String usernameTentado, String motivo) {
        // Como o usuário não logou, o nome dele será o que ele digitou no campo de login.
        // Adapte o método abaixo para a forma como o seu AuditoriaService salva os dados.
        auditService.saveLog(
                AuditAction.LOGIN_FAILED,
                "Autenticação",
                usernameTentado, // Salvamos o que o invasor tentou digitar
                usernameTentado, // Usuário responsável pela ação (ele mesmo)
                motivo
        );
    }
}
