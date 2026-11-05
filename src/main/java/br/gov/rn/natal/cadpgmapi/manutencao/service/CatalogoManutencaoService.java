package br.gov.rn.natal.cadpgmapi.manutencao.service;

import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.CategoriaDemanda;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.Subcategoria;
import br.gov.rn.natal.cadpgmapi.manutencao.domain.entity.TipoServico;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.CategoriaDemandaRequestDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.SubcategoriaRequestDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.request.TipoServicoRequestDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.CategoriaDemandaResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.SubcategoriaResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.dto.response.TipoServicoResponseDTO;
import br.gov.rn.natal.cadpgmapi.manutencao.repository.CategoriaDemandaRepository;
import br.gov.rn.natal.cadpgmapi.manutencao.repository.SubcategoriaRepository;
import br.gov.rn.natal.cadpgmapi.manutencao.repository.TipoServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoManutencaoService {

    private final CategoriaDemandaRepository categoriaRepository;
    private final TipoServicoRepository tipoServicoRepository;
    private final SubcategoriaRepository subcategoriaRepository;

    /*
    LISTAR TODAS AS CATEGORIAS (ATIVAS E INATIVAS) PAGINADAS
    */
    @Transactional(readOnly = true)
    public Page<CategoriaDemandaResponseDTO> listarCategoriasPaginada(Pageable pageable) {
        return categoriaRepository.findAll(pageable)
                .map(this::toCategoriaDTO);
    }

    /*
     LISTA AS CATEGORIAS ATIVAS
     */
    @Transactional(readOnly = true)
    public List<CategoriaDemandaResponseDTO> listarCategoriasAtivas() {
        return categoriaRepository.findByAtivoTrueOrderByOrdemExibicaoAscNomeAsc().stream()
                .map(this::toCategoriaDTO)
                .toList();
    }

    /*
     LISTA AS SUBCATEGORIAS ATIVAS
     */
    @Transactional(readOnly = true)
    public List<SubcategoriaResponseDTO> listarSubcategoriasAtivasPorCategoria(Integer categoriaId) {
        return subcategoriaRepository.findByCategoriaIdAndAtivoTrueOrderByOrdemExibicaoAscNomeAsc(categoriaId)
                .stream()
                .map(this::toSubcategoriaDTO)
                .toList();
    }

    /*
    LISTAR TODAS AS SUBCATEGORIAS (ATIVAS E INATIVAS) PAGINADAS
    */
    @Transactional(readOnly = true)
    public Page<SubcategoriaResponseDTO> listarSubcategoriasPorCategoriaPaginada(Integer categoriaId, Pageable pageable) {
        return subcategoriaRepository.findByCategoriaId(categoriaId, pageable)
                .map(this::toSubcategoriaDTO);
    }

    /*
    LISTA OS TIPOS DE SERVIÇOS ATIVOS POR SUBCATEGORIA
   */
    @Transactional(readOnly = true)
    public List<TipoServicoResponseDTO> listarTiposAtivosPorSubcategoria(Integer subcategoriaId) {
        return tipoServicoRepository.findBySubcategoriaIdAndAtivoTrueOrderByOrdemExibicaoAscNomeAsc(subcategoriaId)
                .stream()
                .map(this::toTipoDTO)
                .toList();
    }

    /*
    LISTA TODOS OS TIPOS DE SERVIÇOS (ATIVOS E INATIVOS) POR CATEGORIA PAGINADOS
    */
    @Transactional(readOnly = true)
    public Page<TipoServicoResponseDTO> listarTiposPorSubcategoriaPaginada(Integer subcategoriaId, Pageable pageable) {
        return tipoServicoRepository.findBySubcategoriaId(subcategoriaId, pageable)
                .map(this::toTipoDTO);
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
    public SubcategoriaResponseDTO criarSubcategoria(SubcategoriaRequestDTO dto) {
        CategoriaDemanda categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " + dto.categoriaId()));

        if (subcategoriaRepository.existsByCategoriaIdAndNomeIgnoreCase(dto.categoriaId(), dto.nome().trim())) {
            throw new BusinessException("Já existe uma subcategoria cadastrada com o nome '" + dto.nome().trim() + "' nesta categoria.");
        }
        Subcategoria sub = Subcategoria.builder()
                .categoria(categoria)
                .nome(dto.nome().trim())
                .descricao(dto.descricao())
                .ordemExibicao(dto.ordemExibicao() != null ? dto.ordemExibicao() : 0)
                .ativo(dto.ativo() != null ? dto.ativo() : true)
                .prazoEstimadoDias(dto.prazoEstimadoDias())
                .requerTombamento(dto.requerTombamento() != null ? dto.requerTombamento() : false)
                .build();
        return toSubcategoriaDTO(subcategoriaRepository.save(sub));
    }
    @Transactional
    public SubcategoriaResponseDTO atualizarSubcategoria(Integer id, SubcategoriaRequestDTO dto) {
        Subcategoria sub = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategoria não encontrada com o ID: " + id));
        if (subcategoriaRepository.existsByCategoriaIdAndNomeIgnoreCaseAndIdNot(sub.getCategoria().getId(), dto.nome().trim(), id)) {
            throw new BusinessException("Já existe outra subcategoria nesta categoria com o nome '" + dto.nome().trim() + "'.");
        }
        sub.setNome(dto.nome().trim());
        sub.setDescricao(dto.descricao());
        if (dto.ordemExibicao() != null) sub.setOrdemExibicao(dto.ordemExibicao());
        if (dto.ativo() != null) sub.setAtivo(dto.ativo());
        if (dto.prazoEstimadoDias() != null) sub.setPrazoEstimadoDias(dto.prazoEstimadoDias());
        if (dto.requerTombamento() != null) sub.setRequerTombamento(dto.requerTombamento());
        return toSubcategoriaDTO(subcategoriaRepository.save(sub));
    }
    @Transactional
    public void alterarStatusSubcategoria(Integer id, boolean ativo) {
        Subcategoria sub = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategoria não encontrada com o ID: " + id));
        sub.setAtivo(ativo);
        subcategoriaRepository.save(sub);
    }

    @Transactional
    public TipoServicoResponseDTO criarTipoServico(TipoServicoRequestDTO dto) {
        Subcategoria subcategoria = subcategoriaRepository.findById(dto.subcategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subcategoria não encontrada com o ID: " + dto.subcategoriaId())
                );

        if (tipoServicoRepository.existsBySubcategoriaIdAndNomeIgnoreCase(
                dto.subcategoriaId(),
                dto.nome().trim()
        )) {
            throw new BusinessException(
                    "Já existe um tipo de serviço com o nome '" + dto.nome().trim() + "' nesta subcategoria.");
        }

        TipoServico tipo = TipoServico.builder()
                .subcategoria(subcategoria)
                .categoria(subcategoria.getCategoria())
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

        Subcategoria subcategoria = subcategoriaRepository.findById(dto.subcategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subcategoria não encontrada com o ID: " + dto.subcategoriaId())
                );


        if (tipoServicoRepository.existsBySubcategoriaIdAndNomeIgnoreCaseAndIdNot(dto.subcategoriaId(), dto.nome().trim(), id)) {
            throw new BusinessException("Já existe outro serviço nesta categoria com o nome '" + dto.nome().trim() + "'.");
        }

        tipo.setSubcategoria(subcategoria);
        tipo.setCategoria(subcategoria.getCategoria());
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
        return new CategoriaDemandaResponseDTO(
                cat.getId(),
                cat.getNome(),
                cat.getDescricao(),
                cat.isAtivo(),
                cat.getOrdemExibicao()
        );
    }

    private TipoServicoResponseDTO toTipoDTO(TipoServico tipo) {
        return new TipoServicoResponseDTO(
                tipo.getId(),
                tipo.getCategoria().getId(),
                tipo.getCategoria().getNome(),
                tipo.getSubcategoria().getId(),
                tipo.getSubcategoria().getNome(),
                tipo.getNome(),
                tipo.getDescricao(),
                tipo.isAtivo(),
                tipo.getOrdemExibicao()
        );
    }

    /**
     * Mapeia a entidade Subcategoria para o Record SubcategoriaResponseDTO,
     * navegando pelo relacionamento com CategoriaDemanda de forma segura.
     */
    private SubcategoriaResponseDTO toSubcategoriaDTO(Subcategoria sub) {
        return new SubcategoriaResponseDTO(
                sub.getId(),
                sub.getCategoria().getId(),
                sub.getCategoria().getNome(),
                sub.getNome(),
                sub.getDescricao(),
                sub.isAtivo(),
                sub.getOrdemExibicao(),
                sub.getPrazoEstimadoDias(),
                sub.isRequerTombamento()
        );
    }
}
