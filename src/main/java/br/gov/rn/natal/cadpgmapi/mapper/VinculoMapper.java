package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.VinculoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.VinculoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Vinculo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VinculoMapper {
    Vinculo toEntity(VinculoRequestDTO dto);
    VinculoResponseDTO toDto(Vinculo entity);

    void updateEntityFromDTO(
            @MappingTarget
            Vinculo entity,
            VinculoRequestDTO dto
    );

    List<VinculoResponseDTO> toDtoList(List<Vinculo> entities);
}
