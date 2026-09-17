package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.ProcuradorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Procurador;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProcuradorMapper extends BaseMapper<
        Procurador, ProcuradorRequestDTO, ProcuradorResponseDTO> {

    @Override
    @Mapping(target = "dataExpiracao", expression = "java(entity.getDataExpiracao())")
    ProcuradorResponseDTO toDto(Procurador entity);
}

