package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.VinculoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.VinculoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Vinculo;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VinculoMapper extends BaseMapper<Vinculo, VinculoRequestDTO, VinculoResponseDTO> {
}
