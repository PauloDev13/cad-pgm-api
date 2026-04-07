package br.gov.rn.natal.cadpgmapi.auth.dto;


import java.util.Set;

public record LoginResponseDTO(
        String userName,
        Set<String> roles
//        String token
//        String type // Padrão de mercado: "Bearer"
) {
}
