package br.gov.rn.natal.cadpgmapi.dto.response;

public record UsuarioRegisterResponseDTO(
        Integer id,
        String name,
        String userName,
        String email
) {
}
