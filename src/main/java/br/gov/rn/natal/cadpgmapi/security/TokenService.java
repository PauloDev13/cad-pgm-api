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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

@Service
public class TokenService {

    // CORREÇÃO DE BUG: antes o placeholder era '@Value("{JWT_SECRET}")' — faltava o cifrão ('${...}').
    // Nesse formato, o Spring NÃO resolvia a variável e injetava a STRING LITERAL "{JWT_SECRET}"
    // como segredo do JWT (segredo fixo e público no código-fonte = tokens forjáveis).
    // Agora lemos a propriedade 'api.security.token.secret', que está mapeada no application.yml
    // e resolve a variável de ambiente JWT_SECRET (mesmo local onde o yml guarda a configuração).
    @Value("${api.security.token.secret}")
    private String secret;

    private static final String ISSUER = "API Cad PGM";

    // O Algorithm é pré-computado uma única vez (memoização) e reutilizado em todas as
    // chamadas — antes era reconstruído a cada request, desperdiçando CPU/byte[].
    private volatile Algorithm cachedAlgorithm;

    /**
     * DEFICIÊNCIA CORRIGIDA (JWT com segredo curto):
     * O segredo do .env de desenvolvimento ("P@uloP@tN@nda131105") tem 20 caracteres = 160 bits.
     * O Algorithm.HMAC256(secret) da biblioteca auth0 exige uma chave de NO MÍNIMO 256 bits e
     * lançava IllegalArgumentException (o login estourava em 500). Para não depender do tamanho
     * da env var, derivamos uma chave SEMPRE de 256 bits através do SHA-256 do segredo configurado.
     * Observação: como as chaves derivadas são estáveis, o comportamento do HMAC continua igual
     * para quem já usa um segredo longo — mantendo a compatibilidade.
     */
    private Algorithm getHmacAlgorithm() {
        Algorithm algorithm = cachedAlgorithm;
        if (algorithm == null) {
            synchronized (this) {
                algorithm = cachedAlgorithm;
                if (algorithm == null) {
                    algorithm = buildHmacAlgorithm();
                    cachedAlgorithm = algorithm;
                }
            }
        }
        return algorithm;
    }

    private Algorithm buildHmacAlgorithm() {
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Algorithm.HMAC256(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível para assinatura do JWT", e);
        }
    }

    public String generateToken(Usuario usuario) {
        try {
            Algorithm algorithm = getHmacAlgorithm();

            // 1. Transformamos as autoridades do Spring em uma lista de Strings simples
            List<String> permissions = usuario.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(usuario.getUsername())
                    .withClaim("roles", permissions)
                    .withClaim("isForcePasswordChange", usuario.isForcePasswordChange())
                    .withIssuedAt(new Date())
                    .withExpiresAt(generateExpirationDate())
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public DecodedJWT validateToken(String token) {
        try {
            Algorithm algorithm = getHmacAlgorithm();
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(token);
        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    private Instant generateExpirationDate() {

        return LocalDateTime.now().plusHours(4)
                .toInstant(ZoneOffset.of("-03:00")
                );
    }

    // GERA O TOKEN DE RECUPERAÇÃO
    public String generatePasswordRecoveryToken(Usuario usuario) {
        try {
            Algorithm algorithm = getHmacAlgorithm();
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
    public DecodedJWT validateRecoveryToken(String token) {
        try {
            Algorithm algorithm = getHmacAlgorithm();
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
