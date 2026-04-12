package br.gov.rn.natal.cadpgmapi.auth.service;

import br.gov.rn.natal.cadpgmapi.auth.dto.request.ForceChangePasswordRequestDTO;
import br.gov.rn.natal.cadpgmapi.auth.dto.request.LoginRequestDTO;
import br.gov.rn.natal.cadpgmapi.auth.dto.response.LoginResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public LoginResponseDTO authenticate(LoginRequestDTO dto) {
        // Busca o usuário pelo Login (UserName)
        // Usamos a mesma mensagem genérica para não dar dicas a invasores se o login existe ou não
        Usuario usuario = usuarioRepository.findByUserName(dto.login())
                .orElseThrow(() -> new BusinessException("Usuário ou senha inválidos."));

        // Valida se o usuário está ativo
        if (!usuario.isActivated()) {
            throw new BusinessException("Usuário inativo. Procure o administrador.");
        }

        // Valida a senha (Temporário em texto puro)
        // TODO: Fase 2 - Substituir por passwordEncoder.matches(dto.password(), usuário.getPassword())
        if (!usuario.getPassword().equals(dto.password())) {
            throw new BusinessException("Usuário ou senha inválidos.");
        }

        // Autenticação com sucesso! Retorna o token Fake
        return new LoginResponseDTO(
                usuario.getUserName(),
                usuario.getPermissions(),
                usuario.isForcePasswordChange()
        );
    }

    // MÉTODOS PARA RESET DE SENHA DO USUÁRIO PELO ADMINISTRADOR

    @Transactional
    public void finalizeRequiredPasswordChange(ForceChangePasswordRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByUserName(dto.userName())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        // TODO: passwordEncoder.encode(dto.novaSenha())
        usuario.setPassword(dto.newPassword());
        usuario.setForcePasswordChange(false); // Libera o acesso normal
        usuarioRepository.save(usuario);
    }
}
