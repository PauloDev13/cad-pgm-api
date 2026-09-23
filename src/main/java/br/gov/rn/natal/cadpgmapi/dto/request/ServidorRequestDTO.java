package br.gov.rn.natal.cadpgmapi.dto.request;

import br.gov.rn.natal.cadpgmapi.enums.TipoAtividade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;
@Schema(description = "Dados para criação ou atualização de um Servidor")
public record ServidorRequestDTO(
        @Schema(description = "Nome completo do servidor", example = "Carlos Alberto de Menezes")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150)
        String nome,

        @Schema(description = "Matrícula do servidor", example = "55025-8, T127")
        @NotBlank(message = "A matrícula é obrigatória")
        @Size(max = 50)
        String matricula,

        @Schema(description = "CPF do servidor", example = "123.456.789-01")
        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
        String cpf,
        @Schema(description = "Data de nascimento do servidor", example = "10/02/1991")
        LocalDate dataNascimento,
        @Schema(description = "Gênero do servidor", example = "Feminino")
        @Size(max = 20)
        String genero,

        @Schema(description = "Telefone do servidor", example = "(84)9865-2639")
        @Size(max = 20)
        String telefone,
        @Schema(description = "E-mail pessoal do servidor", example = "carlos@gmail.com")
        @NotBlank(message = "O e-mail pessoal é obrigatório")
        @Email(message = "E-mail pessoal inválido")
        @Size(max = 100)
        String emailPessoal,
        @Schema(description = "E-mail institucional do servidor", example = "carlos@rn.gov.br")
        @Email(message = "E-mail institucional inválido")
        @Size(max = 100)
        String emailInstitucional,
        @Schema(description = "Endereço completo do servidor", example = "Rua Atalaia, 79, Centro - Natal/RN")
        @Size(max = 255)
        String endereco,
        @Schema(description = "Nome do Pai e Mãe do servidor", example = "José Silva e Maria Silva")
        @Size(max = 255)
        String filiacao,

        @Schema(description = "Tipo de atividade do servidor", example = "Presencial, Remoto")
        TipoAtividade tipoAtividade,

        // IDs das relações N:1
        @Schema(description = "Identificação do cargo", example = "1, 2")
        @NotNull(message = "O cargo é obrigatório")
        Integer cargoId,
        @Schema(description = "Identificação do setor", example = "1, 2")
        @NotNull(message = "O setor é obrigatório")
        Integer setorId,
        @Schema(description = "Identificação da lotação", example = "1, 2")
        @NotNull(message = "A lotação é obrigatória")
        Integer lotacaoId,
        @Schema(description = "Identificação do vínculo", example = "1, 2")
        @NotNull(message = "O vínculo é obrigatório")
        Integer vinculoId,
        @Schema(description = "Identificação do status", example = "1, 2")
        @NotNull(message = "O status é obrigatório")
        Integer statusId,

        // IDs das relações N:N
        Set<Integer>sistemaIds,
        Set<Integer> aliasIds,
        Set<Integer> procuradorIds

) {
}
