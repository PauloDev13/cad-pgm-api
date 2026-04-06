package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.UsuarioRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.UsuarioResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Usuario;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper extends BaseMapper<Usuario, UsuarioRequestDTO, UsuarioResponseDTO> {
}
