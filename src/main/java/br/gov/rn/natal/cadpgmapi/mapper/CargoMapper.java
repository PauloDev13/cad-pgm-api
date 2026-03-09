package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.CargoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.CargoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Cargo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CargoMapper {
    Cargo toEntity(CargoRequestDTO dto);
    CargoResponseDTO toDto(Cargo entity);

    void updateEntityFromDTO(
            @MappingTarget
            Cargo entity,
            CargoRequestDTO dto
    );
}
