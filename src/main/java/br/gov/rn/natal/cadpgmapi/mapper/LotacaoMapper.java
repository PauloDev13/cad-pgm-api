package br.gov.rn.natal.cadpgmapi.mapper;

import br.gov.rn.natal.cadpgmapi.dto.request.LotacaoRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.LotacaoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Lotacao;
import br.gov.rn.natal.cadpgmapi.mapper.generic.BaseMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LotacaoMapper extends BaseMapper<
        Lotacao, LotacaoRequestDTO, LotacaoResponseDTO> {}
