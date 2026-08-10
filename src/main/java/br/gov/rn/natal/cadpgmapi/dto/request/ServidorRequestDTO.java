package br.gov.rn.natal.cadpgmapi.dto.request;

import br.gov.rn.natal.cadpgmapi.enums.TipoAtividade;
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

        // CORREÇÃO DE BUG (b4.14): o e-mail pessoal é obrigatório na ENTIDADE
        // (nullable = false) mas o DTO o tratava como opcional, gerando erro 500 no banco
        // quando o frontend enviava o campo vazio. Agora a validação acontece na camada HTTP.
        @NotBlank(message = "O e-mail pessoal é obrigatório")
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

        TipoAtividade tipoAtividade,

        // IDs das relações N:1
        @NotNull(message = "O cargo é obrigatório")
        Integer cargoId,

        @NotNull(message = "O setor é obrigatório")
        Integer setorId,

        @NotNull(message = "A lotação é obrigatória")
        Integer lotacaoId,

        // CORREÇÃO DE BUG (item 8): as mensagens estavam TROCADAS entre os dois campos.
        // vinculoId referencia a FK de "vínculo" e statusId a FK de "status" — agora cada
        // campo valida com a mensagem correspondente ao dado que realmente espera.
        @NotNull(message = "O vínculo é obrigatório")
        Integer vinculoId,

        @NotNull(message = "O status é obrigatório")
        Integer statusId,

        // IDs das relações N:N
        Set<Integer>sistemaIds,
        Set<Integer> aliasIds,
        Set<Integer> procuradorIds

) {
}
