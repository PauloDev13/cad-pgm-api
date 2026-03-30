package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.SetorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SetorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Setor;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SetorMapper extends BaseMapper<Setor, SetorRequestDTO, SetorResponseDTO> {
}
