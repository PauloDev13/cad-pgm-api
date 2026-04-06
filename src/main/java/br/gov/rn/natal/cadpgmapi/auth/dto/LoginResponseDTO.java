package br.gov.rn.natal.cadpgmapi.auth.dto;

public record LoginResponseDTO(
        String token,
        String type // Padrão de mercado: "Bearer"
) {
}
