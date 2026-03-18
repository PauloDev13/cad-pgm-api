package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.SistemaRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SistemaResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Sistema;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SistemaMapper {
    Sistema toEntity(SistemaRequestDTO dto);
    SistemaResponseDTO toDto(Sistema entity);

    void updateEntityFromDTO(
            @MappingTarget
            Sistema entity,
            SistemaRequestDTO dto
    );

    List<SistemaResponseDTO> toDtoList(List<Sistema> entities);
}
