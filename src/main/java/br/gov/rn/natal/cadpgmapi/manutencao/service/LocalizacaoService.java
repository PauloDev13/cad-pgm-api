package br.gov.rn.natal.cadpgmapi.manutencao.service;

import br.gov.rn.natal.cadpgmapi.manutencao.domain.enums.AndarCodigo;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.AndarResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.SalaResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.repository.SalaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalizacaoService {

    private final SalaRepository salaRepository;

    @Transactional(readOnly = true)
    public List<AndarResponseDTO> listarAndares() {
        return Arrays.stream(AndarCodigo.values())
                .map(andar -> new AndarResponseDTO(andar, andar.getRotulo()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SalaResponseDTO> listarSalasPorAndar(AndarCodigo andar) {
        return salaRepository.findByAndarAndAtivoTrueOrderByNumeroIdentificacaoAsc(andar)
                .stream()
                .map(sala -> new SalaResponseDTO(
                        sala.getId(),
                        sala.getNumeroIdentificacao(),
                        sala.getAndar(),
                        sala.getAndar().getRotulo(),
                        sala.getDescricao(),
                        sala.isAtivo()
                ))
                .toList();
    }
}
