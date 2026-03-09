package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {
        CargoMapper.class,
        SetorMapper.class,
        LotacaoMapper.class,
        StatusMapper.class,
        VinculoMapper.class
})
public interface ServidorMapper {
    // Mapeia os IDs recebidos no DTO para as entidades de referência do JPA
    @Mapping(source = "cargoId", target = "cargo.id")
    @Mapping(source = "setorId", target = "setor.id")
    @Mapping(source = "lotacaoId", target = "lotacao.id")
    @Mapping(source = "statusId", target = "status.id")
    @Mapping(source = "vinculoId", target = "vinculo.id")
    // Ignorando coleções (NN) para tratamento manual no Service
    @Mapping(target = "sistemas", ignore = true)
    @Mapping(target = "aliases", ignore = true)
    @Mapping(target = "procuradores", ignore = true)

    // Ignorando campos de controle de ciclo de vida (tratados em endpoints específicos)
    @Mapping(target = "dataDesligamento", ignore = true)
    Servidor toEntity(ServidorRequestDTO dto);

    ServidorResponseDTO toDto(Servidor entity);

    // Mapeamento para Update (PUT)
    @Mapping(source = "cargoId", target = "cargo.id")
    @Mapping(source = "setorId", target = "setor.id")
    @Mapping(source = "lotacaoId", target = "lotacao.id")
    @Mapping(source = "statusId", target = "status.id")
    @Mapping(source = "vinculoId", target = "vinculo.id")
    @Mapping(target = "sistemas", ignore = true)
    @Mapping(target = "aliases", ignore = true)
    @Mapping(target = "procuradores", ignore = true)
    @Mapping(target = "dataDesligamento", ignore = true)
    void updateEntityFromDTO(ServidorRequestDTO dto, @MappingTarget Servidor entity);
}
