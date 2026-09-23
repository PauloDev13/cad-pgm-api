package br.gov.rn.natal.cadpgmapi.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@Schema(description = "Representação de um Servidor para a lista de aniversariantes")
public record AniversarianteResponseDTO(
        @Schema(description = "Dia e mês da data de aniversário", example = "04/05")
        String diaMes,
        @Schema(description = "Nome completo do aniversariante", example = "Carlos Alberto de Menezes")
        String nome,
        @Schema(description = "Setor do aniversariante", example = "Departamento de TI")
        String setor,
        @Schema(description = "Cargo do aniversariante", example = "Gerente de Projeto")
        String cargo,
        @Schema(description = "Genero do aniversariante", example = "Feminino")
        String genero
) {
    // CONSTRUTOR AUXILIAR: O Hibernate vai usar este construtor!
    public AniversarianteResponseDTO(
            LocalDate dataNascimento,
            String nome,
            String setor,
            String cargo,
            String genero
    ) {
        this(
                dataNascimento != null ? dataNascimento.format(DateTimeFormatter.ofPattern("dd/MM")) : "",
                nome,
                setor,
                cargo,
                genero
        );
    }
}
