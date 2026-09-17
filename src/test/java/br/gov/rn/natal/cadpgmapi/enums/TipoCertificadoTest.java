package br.gov.rn.natal.cadpgmapi.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TipoCertificadoTest {

    @Test
    @DisplayName("Deve calcular data de expiração para certificado A1 (1 ano)")
    void deveCalcularExpiracaoA1() {
        LocalDateTime expedicao = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
        LocalDateTime expiracao = TipoCertificado.A1.calcularDataExpiracao(expedicao);

        assertNotNull(expiracao);
        assertEquals(LocalDateTime.of(2027, 1, 15, 10, 30, 0), expiracao);
    }

    @Test
    @DisplayName("Deve calcular data de expiração para certificado A3 (3 anos)")
    void deveCalcularExpiracaoA3() {
        LocalDateTime expedicao = LocalDateTime.of(2026, 1, 15, 10, 30, 0);
        LocalDateTime expiracao = TipoCertificado.A3.calcularDataExpiracao(expedicao);

        assertNotNull(expiracao);
        assertEquals(LocalDateTime.of(2029, 1, 15, 10, 30, 0), expiracao);
    }

    @Test
    @DisplayName("Deve retornar null ao calcular expiração com data de expedição nula")
    void deveRetornarNullComDataExpedicaoNula() {
        assertNull(TipoCertificado.A1.calcularDataExpiracao(null));
        assertNull(TipoCertificado.A3.calcularDataExpiracao(null));
    }
}
