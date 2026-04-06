package br.gov.rn.natal.cadpgmapi.dto.response;

import java.util.Set;

public record UsuarioResponseDTO(
        Integer id,
        String name,
        String userName,
        String email,
        boolean activated,
        Set<String> permissions
) {}
