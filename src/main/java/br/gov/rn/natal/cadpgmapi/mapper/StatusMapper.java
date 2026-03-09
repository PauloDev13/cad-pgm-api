package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.StatusRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.StatusResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Status;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StatusMapper {
    Status toEntity(StatusRequestDTO dto);
    StatusResponseDTO toDto(Status entity);

    void updateEntityFromDTO(
            @MappingTarget
            Status entity,
            StatusRequestDTO dto
    );
}
