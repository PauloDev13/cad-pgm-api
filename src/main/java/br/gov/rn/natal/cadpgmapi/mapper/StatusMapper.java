package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.StatusRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.StatusResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Status;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StatusMapper extends BaseMapper<Status, StatusRequestDTO, StatusResponseDTO> {
}
