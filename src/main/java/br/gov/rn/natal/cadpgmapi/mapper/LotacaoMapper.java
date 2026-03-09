package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.LotacaoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.LotacaoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Lotacao;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LotacaoMapper {
    Lotacao toEntity(LotacaoRequestDTO dto);
    LotacaoResponseDTO toDto(Lotacao entity);

    void updateEntityFromDTO(
            @MappingTarget
            Lotacao entity,
            LotacaoRequestDTO dto
    );
}
