package br.gov.rn.natal.cadpgmapi.manutencao.service;

import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.CategoriaDemanda;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.TipoServico;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.CategoriaDemandaRequestDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.TipoServicoRequestDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.CategoriaDemandaResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.TipoServicoResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.repository.CategoriaDemandaRepository;
import br.gov.rn.natal.cadpgmapi.manutencao.repository.ChamadoManutencaoRepository;
import br.gov.rn.natal.cadpgmapi.manutencao.repository.TipoServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoManutencaoService {

    private final CategoriaDemandaRepository categoriaRepository;
    private final TipoServicoRepository tipoServicoRepository;
    private final ChamadoManutencaoRepository chamadoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaDemandaResponseDTO> listarCategoriasAtivas() {
        return categoriaRepository.findByAtivoTrueOrderByOrdemExibicaoAscNomeAsc().stream()
                .map(this::toCategoriaDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TipoServicoResponseDTO> listarTiposPorCategoriaAtivos(Integer categoriaId) {
        return tipoServicoRepository.findByCategoriaIdAndAtivoTrueOrderByOrdemExibicaoAscNomeAsc(categoriaId).stream()
                .map(this::toTipoDTO)
                .toList();
    }

    @Transactional
    public CategoriaDemandaResponseDTO criarCategoria(CategoriaDemandaRequestDTO dto) {
        if (categoriaRepository.existsByNomeIgnoreCase(dto.nome().trim())) {
            throw new BusinessException("Já existe uma categoria cadastrada com o nome '" + dto.nome().trim() + "'.");
        }
        CategoriaDemanda cat = CategoriaDemanda.builder()
                .nome(dto.nome().trim())
                .descricao(dto.descricao())
                .ordemExibicao(dto.ordemExibicao() != null ? dto.ordemExibicao() : 0)
                .ativo(dto.ativo() != null ? dto.ativo() : true)
                .build();
        return toCategoriaDTO(categoriaRepository.save(cat));
    }

    @Transactional
    public CategoriaDemandaResponseDTO atualizarCategoria(Integer id, CategoriaDemandaRequestDTO dto) {
        CategoriaDemanda cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " + id));

        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(dto.nome().trim(), id)) {
            throw new BusinessException("Já existe outra categoria com o nome '" + dto.nome().trim() + "'.");
        }

        cat.setNome(dto.nome().trim());
        cat.setDescricao(dto.descricao());
        if (dto.ordemExibicao() != null) cat.setOrdemExibicao(dto.ordemExibicao());
        if (dto.ativo() != null) cat.setAtivo(dto.ativo());

        return toCategoriaDTO(categoriaRepository.save(cat));
    }

    @Transactional
    public TipoServicoResponseDTO criarTipoServico(TipoServicoRequestDTO dto) {
        CategoriaDemanda cat = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " + dto.categoriaId()));

        if (tipoServicoRepository.existsByCategoriaIdAndNomeIgnoreCase(dto.categoriaId(), dto.nome().trim())) {
            throw new BusinessException("Já existe um tipo de serviço com o nome '" + dto.nome().trim() + "' nesta categoria.");
        }

        TipoServico tipo = TipoServico.builder()
                .categoria(cat)
                .nome(dto.nome().trim())
                .descricao(dto.descricao())
                .ordemExibicao(dto.ordemExibicao() != null ? dto.ordemExibicao() : 0)
                .ativo(dto.ativo() != null ? dto.ativo() : true)
                .build();

        return toTipoDTO(tipoServicoRepository.save(tipo));
    }

    @Transactional
    public TipoServicoResponseDTO atualizarTipoServico(Integer id, TipoServicoRequestDTO dto) {
        TipoServico tipo = tipoServicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de serviço não encontrado com o ID: " + id));

        if (tipoServicoRepository.existsByCategoriaIdAndNomeIgnoreCaseAndIdNot(tipo.getCategoria().getId(), dto.nome().trim(), id)) {
            throw new BusinessException("Já existe outro serviço nesta categoria com o nome '" + dto.nome().trim() + "'.");
        }

        tipo.setNome(dto.nome().trim());
        tipo.setDescricao(dto.descricao());
        if (dto.ordemExibicao() != null) tipo.setOrdemExibicao(dto.ordemExibicao());
        if (dto.ativo() != null) tipo.setAtivo(dto.ativo());

        return toTipoDTO(tipoServicoRepository.save(tipo));
    }

    @Transactional
    public void alterarStatusTipoServico(Integer id, boolean ativo) {
        TipoServico tipo = tipoServicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de serviço não encontrado com o ID: " + id));
        tipo.setAtivo(ativo);
        tipoServicoRepository.save(tipo);
    }

    private CategoriaDemandaResponseDTO toCategoriaDTO(CategoriaDemanda cat) {
        return new CategoriaDemandaResponseDTO(cat.getId(), cat.getNome(), cat.getDescricao(), cat.isAtivo(), cat.getOrdemExibicao());
    }

    private TipoServicoResponseDTO toTipoDTO(TipoServico tipo) {
        return new TipoServicoResponseDTO(
                tipo.getId(),
                tipo.getCategoria().getId(),
                tipo.getCategoria().getNome(),
                tipo.getNome(),
                tipo.getDescricao(),
                tipo.isAtivo(),
                tipo.getOrdemExibicao()
        );
    }
}
