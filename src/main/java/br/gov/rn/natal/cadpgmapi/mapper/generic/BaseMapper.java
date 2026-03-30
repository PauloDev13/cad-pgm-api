package br.gov.rn.natal.cadpgmapi.mapper.generic;

import org.mapstruct.MappingTarget;

import java.util.List;

public interface BaseMapper<E, Req, Res> {
    E toEntity(Req dto);
    Res toDto(E entity);

    // O @MappingTarget é essencial para o méthod de UPDATE do CRUD.
    // Ele pega os dados do DTO e joga numa entidade que já existe,
    // sem precisar criar uma nova instância (o que perderia o ID e o tracking do Hibernate).
    void updateEntityFromDTO(@MappingTarget E entity, Req dto);
    List<Res> toDtoList(List<E> entities);
}
