package br.gov.rn.natal.cadpgmapi.dashboard.dto.response;

// DTO para os itens individuais do gráfico
public record GraphItemDTO(
        String label,
        Long quantity
) {}

