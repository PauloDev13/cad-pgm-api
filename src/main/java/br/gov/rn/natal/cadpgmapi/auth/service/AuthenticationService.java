package br.gov.rn.natal.cadpgmapi.auth.service;

import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

         //Vai no MariaDB, busca o usuário pelo login. Se não achar, joga o erro padrão do Spring.
        return usuarioRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário (<strong>" + username +
                        "</strong>) não encontrado."));
    }
}
