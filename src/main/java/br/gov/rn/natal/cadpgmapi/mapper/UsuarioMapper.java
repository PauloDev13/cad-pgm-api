package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.UsuarioRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.UsuarioResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper extends BaseMapper<Usuario, UsuarioRequestDTO, UsuarioResponseDTO> {
    // O mapeamento foi necessário porque após a implementação do Security,
    // é obrigatório ter o método getUsername com N minúsculo. Como nossa entidade
    // Usuário tem o atributo userName com N maiúsculo, os dados desse atributo
    // estavam retornando nulo.
    @Override
    @Mapping(source = "username", target = "userName")
    UsuarioResponseDTO toDto(Usuario entity);
}
