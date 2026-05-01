package br.gov.rn.natal.cadpgmapi.security;

import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.repository.UsuarioRepository;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Pega o token do cabeçalho da requisição
        var token = this.recoverToken(request);

        if (token != null) {
            // 2. Valida o token

            DecodedJWT decodedJWT = tokenService.validarToken(token);

            if (decodedJWT != null) {
                // 3. Se o objeto decodedJWT não for nulo, extrai o username
                // (se o token for falso ou expirado, retorna nulo)
                String username = decodedJWT.getSubject();

                // 4. Busca o usuário no banco de dados
                Usuario usuario = usuarioRepository.findByUserName(username)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

                // 5. Cria o objeto de autenticação que o Spring Security entende
                var authentication = new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        usuario.getAuthorities()
                );

                // 6. Colocamos o objeto completo do JWT nos detalhes da autenticação.
                // É aqui que o AuditAspect vai buscar o 'iat'
                authentication.setDetails(decodedJWT);

                // 7. Salva a autenticação no contexto do Spring (Libera a catraca!)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 6. Passa a requisição para frente (para o próximo filtro ou para o Controller)
        filterChain.doFilter(request, response);
    }

    // Méthod auxiliar para extrair o token do cabeçalho
    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;

        // O token vem no formato "Bearer eyJhbGci...", então nós removemos a palavra "Bearer "
        return authHeader.replace("Bearer ", "");
    }
}
