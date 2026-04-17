package br.gov.rn.natal.cadpgmapi.auth.service;

import br.gov.rn.natal.cadpgmapi.auth.dto.request.ForceChangePasswordRequestDTO;
import br.gov.rn.natal.cadpgmapi.auth.dto.request.LoginRequestDTO;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import br.gov.rn.natal.cadpgmapi.security.TokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    public String authenticate(LoginRequestDTO dto) {
        // 1. Criamos um "envelope" padronizado do Spring com as credenciais cruas que o usuário digitou
        var credenciais = new UsernamePasswordAuthenticationToken(dto.userName(), dto.password());

        try{
            // 3. Se o código chegou nesta linha, significa que o login e senha estão corretos!
            var authentication = authenticationManager.authenticate(credenciais);
            // Extraímos o nosso usuário de dentro do objeto de autenticação
            var usuarioAutenticado = (Usuario) authentication.getPrincipal();

            // 4. Pedimos para a nossa fábrica gerar a Pulseira VIP (JWT) para este usuário e a retornamos
            return tokenService.gerarToken(usuarioAutenticado);


        }catch (AuthenticationException e) {
            // Se a senha estiver errada ou o usuário não existir, o Java cai direto aqui!
            throw new BusinessException("Usuário ou senha inválidos.");
        }

    }

    // MÉTODOS PARA RESET DE SENHA DO USUÁRIO PELO ADMINISTRADOR
    @Transactional
    public void finalizeRequiredPasswordChange(ForceChangePasswordRequestDTO dto) {
        // 1. Extraímos o login (userName) diretamente do Token JWT que está logado no momento!
        String userNameLogado = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Buscamos o usuário no banco usando a identidade do Token
        Usuario usuario = usuarioRepository.findByUserName(userNameLogado)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        // 2. Buscamos o usuário no banco usando a identidade do Token
        usuario.setPassword(passwordEncoder.encode(dto.newPassword()));
        usuario.setForcePasswordChange(false); // Libera o acesso normal
        usuarioRepository.save(usuario);
    }
}
