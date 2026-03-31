package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.*;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
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
public interface ServidorMapper extends BaseMapper<Servidor, ServidorRequestDTO, ServidorResponseDTO> {
    // Mapeia os IDs recebidos no DTO para as entidades de referência do JPA
    @Override
    @Mapping(source = "cargoId", target = "cargo")
    @Mapping(source = "setorId", target = "setor")
    @Mapping(source = "lotacaoId", target = "lotacao")
    @Mapping(source = "statusId", target = "status")
    @Mapping(source = "vinculoId", target = "vinculo")
    // Ignorando coleções (NN) para tratamento manual no Service
    @Mapping(target = "sistemas", ignore = true)
    @Mapping(target = "aliases", ignore = true)
    @Mapping(target = "procuradores", ignore = true)

    // Ignorando campos de controle de ciclo de vida (tratados em endpoints específicos)
    @Mapping(target = "dataDesligamento", ignore = true)
    Servidor toEntity(ServidorRequestDTO dto);

    @Override
    ServidorResponseDTO toDto(Servidor entity);

    // Mapeamento para Update (PUT)
    @Override
    @Mapping(source = "cargoId", target = "cargo")
    @Mapping(source = "setorId", target = "setor")
    @Mapping(source = "lotacaoId", target = "lotacao")
    @Mapping(source = "statusId", target = "status")
    @Mapping(source = "vinculoId", target = "vinculo")
    @Mapping(target = "sistemas", ignore = true)
    @Mapping(target = "aliases", ignore = true)
    @Mapping(target = "procuradores", ignore = true)
    @Mapping(target = "dataDesligamento", ignore = true)
    void updateEntityFromDTO(@MappingTarget Servidor entity, ServidorRequestDTO dto);

    // --- Métodos Auxiliares para Conversão de IDs em Entidades --- //
    default Cargo mapCargo(Integer id) {
        if (id == null) return null;
        Cargo cargo = new Cargo();
        cargo.setId(id);
        return cargo;
    }

    default Setor mapSetor(Integer id) {
        if (id == null) return null;
        Setor setor = new Setor();
        setor.setId(id);
        return setor;
    }

    default Lotacao mapLotacao(Integer id) {
        if (id == null) return null;
        Lotacao lotacao = new Lotacao();
        lotacao.setId(id);
        return lotacao;
    }

    default Status mapStatus(Integer id) {
        if (id == null) return null;
        Status status = new Status();
        status.setId(id);
        return status;
    }

    default Vinculo mapVinculo(Integer id) {
        if (id == null) return null;
        Vinculo vinculo = new Vinculo();
        vinculo.setId(id);
        return vinculo;
    }
}

