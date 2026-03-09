package br.gov.rn.natal.cadpgmapi.dto.response;

import java.time.LocalDate;
import java.util.Set;

public record ServidorResponseDTO(
        Integer id,
        String nome,
        String matricula,
        String cpf,
        LocalDate dataNascimento,
        String genero,
        String telefone,
        String emailPessoal,
        String emailInstitucional,
        String endereco,
        String filiacao,

        // Relações N:1 devolvidas como DTOs
        CargoResponseDTO cargo,
        LotacaoResponseDTO lotacao,
        SetorResponseDTO setor,
        StatusResponseDTO status,
        VinculoResponseDTO vinculo,

        // Relações N:N devolvidas como listas de DTOs
        Set<SistemaResponseDTO> sistemas,
        Set<AliasResponseDTO> aliases,
        Set<ProcuradorResponseDTO> procuradores
) {
}
