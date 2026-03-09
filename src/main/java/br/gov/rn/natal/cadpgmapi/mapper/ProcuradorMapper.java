package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.ProcuradorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Procurador;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProcuradorMapper {
    Procurador toEntity(ProcuradorRequestDTO dto);
    ProcuradorResponseDTO toDto(Procurador entity);

    void updateEntityFromDTO(
            @MappingTarget
            Procurador entity,
            ProcuradorRequestDTO dto
    );
}
