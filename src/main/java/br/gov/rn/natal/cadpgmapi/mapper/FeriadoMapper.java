package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.FeriadoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.FeriadoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Feriado;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeriadoMapper extends BaseMapper<Feriado, FeriadoRequestDTO, FeriadoResponseDTO> {
}
