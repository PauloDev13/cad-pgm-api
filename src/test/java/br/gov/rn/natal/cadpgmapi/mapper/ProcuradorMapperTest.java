package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.ProcuradorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Procurador;
import br.gov.rn.natal.cadpgmapi.enums.TipoCertificado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProcuradorMapperTest {

    private final ProcuradorMapper mapper = Mappers.getMapper(ProcuradorMapper.class);

    @Test
    @DisplayName("Deve mapear RequestDTO para Entidade Procurador corretamente")
    void deveMapearRequestDtoParaEntidade() {
        LocalDateTime expedicao = LocalDateTime.of(2026, 1, 15, 9, 0, 0);
        ProcuradorRequestDTO dto = new ProcuradorRequestDTO(
                "Dr. Roberto Dias",
                TipoCertificado.A1,
                expedicao
        );

        Procurador entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("Dr. Roberto Dias", entity.getNome());
        assertEquals(TipoCertificado.A1, entity.getTipoCertificado());
        assertEquals(expedicao, entity.getDataExpedicao());
        assertEquals(LocalDateTime.of(2027, 1, 15, 9, 0, 0), entity.getDataExpiracao());
    }

    @Test
    @DisplayName("Deve mapear Entidade Procurador para ResponseDTO com data de expiração calculada (A1 = +1 ano)")
    void deveMapearEntidadeParaResponseDtoComExpiracaoA1() {
        LocalDateTime expedicao = LocalDateTime.of(2026, 5, 10, 14, 0, 0);
        Procurador entity = Procurador.builder()
                .id(1)
                .nome("Dra. Vanessa Lima")
                .tipoCertificado(TipoCertificado.A1)
                .dataExpedicao(expedicao)
                .build();

        ProcuradorResponseDTO dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(1, dto.id());
        assertEquals("Dra. Vanessa Lima", dto.nome());
        assertEquals(TipoCertificado.A1, dto.tipoCertificado());
        assertEquals(expedicao, dto.dataExpedicao());
        assertEquals(LocalDateTime.of(2027, 5, 10, 14, 0, 0), dto.dataExpiracao());
    }

    @Test
    @DisplayName("Deve mapear Entidade Procurador para ResponseDTO com data de expiração calculada (A3 = +3 anos)")
    void deveMapearEntidadeParaResponseDtoComExpiracaoA3() {
        LocalDateTime expedicao = LocalDateTime.of(2025, 8, 20, 8, 30, 0);
        Procurador entity = Procurador.builder()
                .id(2)
                .nome("Dr. Marcelo Fontes")
                .tipoCertificado(TipoCertificado.A3)
                .dataExpedicao(expedicao)
                .build();

        ProcuradorResponseDTO dto = mapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(2, dto.id());
        assertEquals("Dr. Marcelo Fontes", dto.nome());
        assertEquals(TipoCertificado.A3, dto.tipoCertificado());
        assertEquals(expedicao, dto.dataExpedicao());
        assertEquals(LocalDateTime.of(2028, 8, 20, 8, 30, 0), dto.dataExpiracao());
    }

    @Test
    @DisplayName("Deve atualizar entidade existente a partir de RequestDTO")
    void deveAtualizarEntidadeExistente() {
        Procurador existing = Procurador.builder()
                .id(10)
                .nome("Nome Antigo")
                .tipoCertificado(TipoCertificado.A1)
                .dataExpedicao(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .build();

        LocalDateTime novaExpedicao = LocalDateTime.of(2026, 3, 1, 10, 0, 0);
        ProcuradorRequestDTO updateDto = new ProcuradorRequestDTO(
                "Nome Atualizado",
                TipoCertificado.A3,
                novaExpedicao
        );

        mapper.updateEntityFromDTO(existing, updateDto);

        assertEquals(10, existing.getId());
        assertEquals("Nome Atualizado", existing.getNome());
        assertEquals(TipoCertificado.A3, existing.getTipoCertificado());
        assertEquals(novaExpedicao, existing.getDataExpedicao());
        assertEquals(LocalDateTime.of(2029, 3, 1, 10, 0, 0), existing.getDataExpiracao());
    }
}
