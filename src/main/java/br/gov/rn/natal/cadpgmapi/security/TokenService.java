package br.gov.rn.natal.cadpgmapi.security;

import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class TokenService {

    @Value("{JWT_SECRET}")
    private String secret;

    private static final String ISSUER = "API Cad PGM";

    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            // 1. Transformamos as autoridades do Spring em uma lista de Strings simples
            List<String> permissions = usuario.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(usuario.getUsername())
                    .withClaim("roles", permissions)
                    .withClaim("isForcePasswordChange", usuario.isForcePasswordChange())
                    .withExpiresAt(gerarDataExpiracao())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String validarToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            return "";
        }
    }

    private Instant gerarDataExpiracao() {

        return LocalDateTime.now().plusHours(2)
                .toInstant(ZoneOffset.of("-03:00")
                );
    }

    // GERA O TOKEN DE RECUPERAÇÃO
    public String gerarTokenRecuperacaoSenha(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(usuario.getEmail()) // Aqui o Subject é o e-mail
                    .withClaim("type", "reset_password") // Identifica que é um token de reset
                    .withClaim("hash", usuario.getPassword()) // O Truque: guardamos a senha atual
                    .withClaim("username", usuario.getUsername())
                    .withExpiresAt(LocalDateTime.now().plusMinutes(30).toInstant(ZoneOffset.of("-03:00")))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT de recuperação", exception);
        }
    }

    // VALIDA E DECODIFICA O TOKEN DE RECUPERAÇÃO
    public DecodedJWT validarTokenRecuperacao(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .withClaim("type", "reset_password") // Garante que não usem token de login aqui
                    .build()
                    .verify(token);
        } catch (JWTVerificationException exception) {
            throw new BusinessException("Token inválido ou expirado.");
        }
    }
}
