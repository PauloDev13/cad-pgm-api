package br.gov.rn.natal.cadpgmapi.auth.mappers;

import br.gov.rn.natal.cadpgmapi.dto.request.UsuarioRegisterRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.UsuarioRegisterResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegisterUserMapper extends BaseMapper<
        Usuario, UsuarioRegisterRequestDTO, UsuarioRegisterResponseDTO> {

    // O mapeamento foi necessário porque após a implementação do Security,
    // é obrigatório ter o método getUsername com N minúsculo. Como nossa entidade
    // Usuário tem o atributo userName com N maiúsculo, os dados desse atributo
    // estavam retornando nulo.
    @Override
    @Mapping(source = "username", target = "userName")
    UsuarioRegisterResponseDTO toDto(Usuario entity);
}
