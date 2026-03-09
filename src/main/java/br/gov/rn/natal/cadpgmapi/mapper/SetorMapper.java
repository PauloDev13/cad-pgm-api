package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.SetorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.SetorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Setor;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SetorMapper {
    Setor toEntity(SetorRequestDTO dto);
    SetorResponseDTO toDto(Setor entity);

    void updateEntityFromDTO(
            @MappingTarget
            Setor entity,
            SetorRequestDTO dto
    );
}
