package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.AliasRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.AliasResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Alias;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AliasMapper {
    Alias toEntity(AliasRequestDTO dto);

    AliasResponseDTO toDto(Alias entity);

    void updateEntityFromDTO(
            @MappingTarget
            Alias entity,
            AliasRequestDTO dto
    );

    List<AliasResponseDTO> toDtoList(List<Alias> entities);
}
