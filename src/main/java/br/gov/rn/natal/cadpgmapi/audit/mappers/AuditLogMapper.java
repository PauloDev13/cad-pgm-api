package br.gov.rn.natal.cadpgmapi.audit.mappers;

import br.gov.rn.natal.cadpgmapi.audit.dtos.AuditLogRequestDTO;
import br.gov.rn.natal.cadpgmapi.audit.dtos.AuditLogResponseDTO;
import br.gov.rn.natal.cadpgmapi.audit.entities.AuditLog;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;

// O componentModel = "spring" avisa ao MapStruct para colocar um @Component
// na classe gerada, permitindo que você a injete com @Autowired nos Controllers
@Mapper(componentModel = "spring")
public interface AuditLogMapper extends BaseMapper<AuditLog, AuditLogRequestDTO, AuditLogResponseDTO> {
}
