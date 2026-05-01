package br.gov.rn.natal.cadpgmapi.audit.mappers;

import br.gov.rn.natal.cadpgmapi.audit.dtos.AuditLogResponseDTO;
import br.gov.rn.natal.cadpgmapi.audit.entities.AuditLog;
import org.mapstruct.Mapper;

// O componentModel = "spring" avisa ao MapStruct para colocar um @Component
// na classe gerada, permitindo que você a injete com @Autowired nos Controllers
@Mapper(componentModel = "spring")
public interface AuditLogMapper{
    /**
     * Converte a Entidade AuditLog para o DTO de Resposta.
     * O MapStruct mapeia os campos com nomes iguais automaticamente.
     * Ele também converte o Enum AcaoAuditoria para String sozinho.
     */
    AuditLogResponseDTO toResponseDTO(AuditLog entity);
}
