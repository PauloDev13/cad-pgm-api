package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.AliasRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.AliasResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Alias;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AliasMapper extends BaseMapper<Alias, AliasRequestDTO, AliasResponseDTO> {
}
