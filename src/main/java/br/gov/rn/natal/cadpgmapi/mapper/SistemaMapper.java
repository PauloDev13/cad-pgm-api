package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.SistemaRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SistemaResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Sistema;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SistemaMapper extends BaseMapper<Sistema, SistemaRequestDTO, SistemaResponseDTO> {
}
