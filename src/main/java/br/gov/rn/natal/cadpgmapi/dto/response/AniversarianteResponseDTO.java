package br.gov.rn.natal.cadpgmapi.dto.response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record AniversarianteResponseDTO(
        String diaMes,
        String nome,
        String setor
) {
    // CONSTRUTOR AUXILIAR: O Hibernate vai usar este construtor!
    public AniversarianteResponseDTO(LocalDate dataNascimento, String nome, String setor) {
        this(
            dataNascimento != null ? dataNascimento.format(DateTimeFormatter.ofPattern("dd/MM")) : "",
            nome,
            setor
        );
    }
}
