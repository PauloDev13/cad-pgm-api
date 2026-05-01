package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.CargoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.CargoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Cargo;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CargoMapper extends BaseMapper<Cargo, CargoRequestDTO, CargoResponseDTO> {
}
