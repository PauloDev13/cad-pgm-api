package br.gov.rn.natal.cadpgmapi.auth.dto.response;


import java.util.Set;

public record LoginResponseDTO(
        // TODO: adicionar os atributos token e type após implementar o JWT
        String userName,
        Set<String> roles,
        boolean forcePasswordChange
) {
}
