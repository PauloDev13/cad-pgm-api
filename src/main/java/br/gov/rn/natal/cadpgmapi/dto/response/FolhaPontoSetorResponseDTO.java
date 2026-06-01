package br.gov.rn.natal.cadpgmapi.dto.response;
import java.util.List;

// Nó externo (O Setor que agrupa os servidores)
public record FolhaPontoSetorResponseDTO(
        String nomeSetor,
        Integer totalServidores,
        List<FolhaPontoServidorDTO> servidores
) {}