package br.gov.rn.natal.cadpgmapi.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;

public record ServidorRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150)
        String nome,

        @NotBlank(message = "A matrícula é obrigatória")
        @Size(max = 50)
        String matricula,

        @NotBlank(message = "O CPF é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
        String cpf,
        LocalDate dataNascimento,

        @Size(max = 20)
        String genero,

        @Size(max = 20)
        String telefone,

        @Email(message = "E-mail pessoal inválido")
        @Size(max = 100)
        String emailPessoal,

        @Email(message = "E-mail institucional inválido")
        @Size(max = 100)
        String emailInstitucional,

        @Size(max = 255)
        String endereco,

        @Size(max = 255)
        String filiacao,

        // IDs das relações N:1
        @NotNull(message = "O cargo é obrigatório")
        Integer cargoId,

        @NotNull(message = "O setor é obrigatório")
        Integer setorId,

        @NotNull(message = "A lotação é obrigatória")
        Integer lotacaoId,

        @NotNull(message = "O status é obrigatório")
        Integer vinculoId,

        @NotNull(message = "O vínculo é obrigatório")
        Integer statusId,

        // IDs das relações N:N
        Set<Integer>sistemaIds,
        Set<Integer> aliasIds,
        Set<Integer> procuradorIds

) {
}
