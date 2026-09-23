package br.gov.rn.natal.cadpgmapi.dto.response;

import br.gov.rn.natal.cadpgmapi.audit.annotations.AuditFriendlyId;
import br.gov.rn.natal.cadpgmapi.enums.TipoAtividade;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
@Schema(description = "Representação de um servidor")
public record ServidorResponseDTO(
        @Schema(description = "Identificador único do vínculo", example = "1")
        Integer id,
        @Schema(description = "Nome completo do servidor", example = "Carlos Alberto de Menezes")
        // Etiqueta para auditoria
        @AuditFriendlyId
        String nome,
        @Schema(description = "Matrícula do servidor", example = "55025-8, T127")
        String matricula,
        @Schema(description = "CPF do servidor", example = "123.456.789-01")
        String cpf,
        @Schema(description = "Data de nascimento do servidor", example = "10/02/1991")
        LocalDate dataNascimento,
        @Schema(description = "Gênero do servidor", example = "Feminino")
        String genero,
        @Schema(description = "Data de nascimento do servidor", example = "10/02/1991")
        String telefone,
        @Schema(description = "E-mail pessoal do servidor", example = "carlos@gmail.com")
        String emailPessoal,
        @Schema(description = "E-mail institucional do servidor", example = "carlos@rn.gov.br")
        String emailInstitucional,
        @Schema(description = "Endereço completo do servidor", example = "Rua Atalaia, 79, Centro - Natal/RN")
        String endereco,
        @Schema(description = "Nome do Pai e Mãe do servidor", example = "José Silva e Maria Silva")
        String filiacao,
        @Schema(description = "Se o servidor foi excluído", example = "False")
        Boolean excluded,
        @Schema(description = "Data da exclusão", example = "01/01/2026")
        LocalDateTime excludedDate,
        @Schema(description = "Caminho da foto do servidor", example = "/fotos/123.png")
        String photoPath,
        @Schema(description = "Tipo de atividade do servidor", example = "Presencial, Remoto")
        TipoAtividade tipoAtividade,

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
