package br.gov.rn.natal.cadpgmapi.security;

import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.exception.UnauthorizedException;
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
import org.springframework.util.StringUtils;
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
            // 2. Valida o token (se for falso/expirado, o validateToken retorna nulo)
            DecodedJWT decodedJWT = tokenService.validateToken(token);

            if (decodedJWT != null) {
                // 3. Extrai o username (subject) do token já assinado e válido
                String username = decodedJWT.getSubject();

                // 4. Busca o usuário no banco — um token válido cujo dono não existe mais
                //    é tratado como não autenticado (401), e não como erro interno (500).
                Usuario usuario = usuarioRepository.findByUserName(username)
                        .orElseThrow(() -> new UnauthorizedException(
                                "Usuário do token não encontrado na base de dados."));

                // 5. Cria o objeto de autenticação que o Spring Security entende
                var authentication = new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        usuario.getAuthorities()
                );

                // 6. Guardamos o JWT nos detalhes — o AuditAspect usa o 'iat' (data de login)
                authentication.setDetails(decodedJWT);

                // 7. Salva a autenticação no contexto do Spring (Libera a catraca!)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 8. Passa a requisição para frente (para o próximo filtro ou para o Controller)
        filterChain.doFilter(request, response);
    }

    // Método auxiliar para extrair o token do cabeçalho Authorization.
    // DEFICIÊNCIA CORRIGIDA: antes usava 'authHeader.replace("Bearer ", "")', que removia a
    // palavra "Bearer " DE QUALQUER posição (inclusive de dentro do token) e também aceitava
    // tokens de esquemas diferentes (ex.: Basic). Agora exigimos o prefixo no início e, se ele
    // não estiver presente (ou o header for vazio), retornamos nulo (requisição segue sem auth).
    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        // O token vem no formato "Bearer eyJhbGci...", então removemos apenas o prefixo "Bearer "
        return authHeader.substring("Bearer ".length());
    }
}
