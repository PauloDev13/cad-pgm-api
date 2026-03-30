package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.CargoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.CargoResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.CargoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Cargo;
import br.gov.rn.natal.cadpgmapi.entity.Cargo;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CargoMapper extends BaseMapper<Cargo, CargoRequestDTO, CargoResponseDTO> {
//    Cargo toEntity(CargoRequestDTO dto);
//    CargoResponseDTO toDto(Cargo entity);
//
//    void updateEntityFromDTO(
//            @MappingTarget
//            Cargo entity,
//            CargoRequestDTO dto
//    );
//
//    List<CargoResponseDTO> toDtoList(List<Cargo> entities);
}
