package br.gov.rn.natal.cadpgmapi.dto.response;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Representação que agrupa os servidores para o relatório da folha de ponto")
public record FolhaPontoSetorResponseDTO(
        @Schema(description = "Identificador único do setor", example = "1")
        Integer idSetor,
        @Schema(description = "Nome do setor", example = "Departamento de TI")
        String nomeSetor,
        @Schema(description = "Total de servidores no relatório", example = "200")
        Integer totalServidores,
        @Schema(description = "Lista dos servidores no relatório", example = "Departamento de TI")
        List<FolhaPontoServidorDTO> servidores
) {}