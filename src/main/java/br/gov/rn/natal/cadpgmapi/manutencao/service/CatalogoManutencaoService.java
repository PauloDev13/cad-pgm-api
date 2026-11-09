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
import br.gov.rn.natal.cadpgmapi.manutencao.repository.CategoriaRepository;
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

    private final CategoriaRepository categoriaRepository;
    private final TipoServicoRepository tipoServicoRepository;
    private final SubcategoriaRepository subcategoriaRepository;

                    // ==============================
                    //          TABELAS
                    // ==============================

    /*
     * LISTA TODAS AS CATEGORIAS (ATIVAS/INATIVAS) COM PAGINAÇÃO (ADMINISTRATIVO)
     */
    @Transactional(readOnly = true)
    public Page<CategoriaDemandaResponseDTO> listarCategoriasPaginada(Pageable pageable) {
        return categoriaRepository.findAll(pageable)
                .map(this::toCategoriaDTO);
    }

    /*
     * LISTA TODAS AS SUBCATEGORIAS (ATIVAS/INATIVAS) COM FILTROS OPCIONAIS E PAGINAÇÃO (ADMINISTRATIVO)
     */
    @Transactional(readOnly = true)
    public Page<SubcategoriaResponseDTO> listarSubcategoriasPaginada(
            Integer categoriaId,
            Pageable pageable) {

        Page<Subcategoria> page;

        if (categoriaId != null) {
            page = subcategoriaRepository.findByCategoriaId(categoriaId, pageable);
        } else {
            page = subcategoriaRepository.findAll(pageable);
        }

        return page.map(this::toSubcategoriaDTO);
    }

    /**
    * LISTA TIPOS DE SERVIÇOS (ATIVOS E INATIVOS) COM FILTROS OPCIONAIS E PAGINAÇÃO (ADMINISTRATIVO)
   */
    @Transactional(readOnly = true)
    public Page<TipoServicoResponseDTO> listarTiposServicosPaginada(
            Integer subcategoriaId,
            Pageable pageable) {

        Page<TipoServico> page;

        if (subcategoriaId != null) {
            page = tipoServicoRepository.findBySubcategoriaId(subcategoriaId, pageable);
        } else {
            page = tipoServicoRepository.findAll(pageable);
        }
        return page.map(this::toTipoDTO);
    }

                        // ==============================
                        //          DROPDOWNS
                        // ==============================

    /*
     LISTA TODAS AS CATEGORIAS ATIVAS (DROPDOWN EM CASCATA - NÍVEL 1)
     */
    @Transactional(readOnly = true)
    public List<CategoriaDemandaResponseDTO> listarCategoriasAtivas() {
        return categoriaRepository.findAllByOrderByOrdemExibicaoAscNomeAsc()
                .stream()
                .map(this::toCategoriaDTO)
                .toList();
    }

    /*
     * LISTA TODAS AS SUBCATEGORIAS ATIVAS POR CATEGORIA (DROPDOWN EM CASCATA - NÍVEL 2)
     */
    @Transactional(readOnly = true)
    public List<SubcategoriaResponseDTO> listarSubcategoriasAtivasPorCategoria(Integer categoriaId) {
        return subcategoriaRepository.findByCategoriaIdAndAtivoTrueOrderByOrdemExibicaoAscNomeAsc(categoriaId)
                .stream()
                .map(this::toSubcategoriaDTO)
                .toList();
    }

    /*
     * LISTA TODAS AS SUBCATEGORIAS ATIVAS POR CATEGORIA (DROPDOWN EM CASCATA - NÍVEL 2)
     */
    @Transactional(readOnly = true)
    public List<SubcategoriaResponseDTO> listarSubcategoriasAtivas() {
        return subcategoriaRepository.findAllByOrderByOrdemExibicaoAscNomeAsc()
                .stream()
                .map(this::toSubcategoriaDTO)
                .toList();
    }

    /**
    * LISTA TOS TIPOS DE SERVIÇOS ATIVOS POR SUBCATEGORIA (DROPDOWN EM CASCATA - NÍVEL 3)
   */
    @Transactional(readOnly = true)
    public List<TipoServicoResponseDTO> listarTiposAtivosPorSubcategoria(Integer subcategoriaId) {
        return tipoServicoRepository.findBySubcategoriaIdAndAtivoTrueOrderByOrdemExibicaoAscNomeAsc(subcategoriaId)
                .stream()
                .map(this::toTipoDTO)
                .toList();
    }

    // ==============================
    //              CREATE
    // ==============================

    /**
    * CRIAR UMA NOVA CATEGORIA
    */
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

    /**
    * CRIAR UMA NOVA SUBCATEGORIA
    */
    @Transactional
    public SubcategoriaResponseDTO criarSubcategoria(SubcategoriaRequestDTO dto) {
        CategoriaDemanda categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " +
                        dto.categoriaId())
                );

            if (!categoria.isAtivo()) {
                throw new BusinessException("Não foi possível cria a Subcategoria '<strong>" + dto.nome().toUpperCase() +
                        "</strong>'.\n Ative a Categoria '<strong>" + categoria.getNome().toUpperCase() +
                        "</strong>' e tente novamente.");
            }

        if (subcategoriaRepository.existsByCategoriaIdAndNomeIgnoreCase(dto.categoriaId(), dto.nome().trim())) {
            throw new BusinessException("Já existe uma subcategoria cadastrada com o nome '" + dto.nome().trim() +
                    "' nesta categoria."
            );
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

    /**
    * CRIA UM NOVO TIPO DE SERVIÇO
    */
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

    // ==============================
    //              UPDATES
    // ==============================

    /*
    * ATUALIZAR UMA CATEGORIA
    */
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

        // Se houve alteração no status ativo através do formulário de edição, dispara a cascata
        if (dto.ativo() != null && dto.ativo() != cat.isAtivo()) {
            alterarStatusCategoria(id, dto.ativo());
        } else {
            categoriaRepository.save(cat);
        }

        return toCategoriaDTO(cat);
    }

    /*
    * ATUALIZAR UMA SUBCATEGORIA
    */
    @Transactional
    public SubcategoriaResponseDTO atualizarSubcategoria(Integer id, SubcategoriaRequestDTO dto) {
        Subcategoria sub = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategoria não encontrada com o ID: " + id));

        CategoriaDemanda categoriaPai = sub.getCategoria();

        if (dto.categoriaId() != null && !dto.categoriaId().equals(categoriaPai.getId())) {
            categoriaPai = categoriaRepository.findById(dto.categoriaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " +
                            dto.categoriaId())
                    );

            sub.setCategoria(categoriaPai);
        }

        if (subcategoriaRepository.existsByCategoriaIdAndNomeIgnoreCaseAndIdNot(
                categoriaPai.getId(), dto.nome().trim(), id)
        ) {
            throw new BusinessException("Já existe outra subcategoria nesta categoria com o nome '" +
                    dto.nome().trim() + "'."
            );
        }

        sub.setNome(dto.nome().trim());
        sub.setDescricao(dto.descricao());

        if (dto.ordemExibicao() != null) sub.setOrdemExibicao(dto.ordemExibicao());
        if (dto.ativo() != null) sub.setAtivo(dto.ativo());
        if (dto.prazoEstimadoDias() != null) sub.setPrazoEstimadoDias(dto.prazoEstimadoDias());
        if (dto.requerTombamento() != null) sub.setRequerTombamento(dto.requerTombamento());

        return toSubcategoriaDTO(subcategoriaRepository.save(sub));
    }

    /**
     * ATUALIZAR UM TIPO DE SERVIÇO
    */
    @Transactional
    public TipoServicoResponseDTO atualizarTipoServico(Integer id, TipoServicoRequestDTO dto) {
        TipoServico tipo = tipoServicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de serviço não encontrado com o ID: " + id));

        Subcategoria subcategoria = subcategoriaRepository.findById(dto.subcategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subcategoria não encontrada com o ID: " + dto.subcategoriaId())
                );


        if (tipoServicoRepository.existsBySubcategoriaIdAndNomeIgnoreCaseAndIdNot(
                dto.subcategoriaId(), dto.nome().trim(), id)) {
            throw new BusinessException(
                    "Já existe outro serviço nesta categoria com o nome '" + dto.nome().trim() + "'.");
        }

        tipo.setSubcategoria(subcategoria);
        tipo.setCategoria(subcategoria.getCategoria());
        tipo.setNome(dto.nome().trim());
        tipo.setDescricao(dto.descricao());

        if (dto.ordemExibicao() != null) tipo.setOrdemExibicao(dto.ordemExibicao());

        if (dto.ativo() != null) tipo.setAtivo(dto.ativo());

        return toTipoDTO(tipoServicoRepository.save(tipo));
    }

    // =================================================
    //              UPDATE ADMINISTRATIVOS
    // =================================================

    /**
     *  ALTERAR STATUS DE CATEGORIA COM PROPAGAÇÃO TRANSACIONAL EM CASCATA
     */
    @Transactional
    public void alterarStatusCategoria(Integer categoriaId, boolean ativo) {
        CategoriaDemanda categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o ID: " + categoriaId));
        if (categoria.isAtivo() == ativo) {
            return;
        }
        categoria.setAtivo(ativo);
        categoriaRepository.save(categoria);
        if (!ativo) {
            // DESABILITAR CATEGORIA
            // 1. Localiza apenas as subcategorias que estão ativas imediatamente antes da operação
            List<Subcategoria> subcategoriasAtivas = subcategoriaRepository.findByCategoriaIdAndAtivoTrue(categoriaId);
            for (Subcategoria sub : subcategoriasAtivas) {
                sub.setAtivo(false);
                sub.setInativadoPorCascata(true); // Marca que foi inativada por cascata
                subcategoriaRepository.save(sub);
                // Propaga cascata para os tipos de serviço ativos desta subcategoria
                List<TipoServico> tiposAtivos = tipoServicoRepository.findBySubcategoriaIdAndAtivoTrue(sub.getId());
                for (TipoServico tipo : tiposAtivos) {
                    tipo.setAtivo(false);
                    tipo.setInativadoPorCascata(true);
                    tipoServicoRepository.save(tipo);
                }
            }
            // Subcategorias que já estavam com ativo == false permanecem com inativadoPorCascata == false
        } else {
            // HABILITAR CATEGORIA
            // 1. Restaura SOMENTE as subcategorias que estão inativadas por cascata
            List<Subcategoria> subcategoriasParaRestaurar =
                    subcategoriaRepository.findByCategoriaIdAndInativadoPorCascataTrue(categoriaId);
            for (Subcategoria sub : subcategoriasParaRestaurar) {
                sub.setAtivo(true);
                sub.setInativadoPorCascata(false);
                subcategoriaRepository.save(sub);
                // Restaura os tipos de serviço que também foram inativados por cascata
                List<TipoServico> tiposParaRestaurar =
                        tipoServicoRepository.findBySubcategoriaIdAndInativadoPorCascataTrue(sub.getId());
                for (TipoServico tipo : tiposParaRestaurar) {
                    tipo.setAtivo(true);
                    tipo.setInativadoPorCascata(false);
                    tipoServicoRepository.save(tipo);
                }
            }
            // Subcategorias que estavam desabilitadas manualmente (inativadoPorCascata == false) permanecem desabilitadas!
        }
    }

    /**
     * ALTERAR O STATUS (ATIVO/INATIVO) DE UMA SUBCATEGORIA
    */
    @Transactional
    public void alterarStatusSubcategoria(Integer id, boolean ativo) {
        Subcategoria sub = subcategoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategoria não encontrada com o ID: " + id));

        if (ativo) {
            if (!sub.getCategoria().isAtivo()) {
                throw new BusinessException("Não é possível habilitar a Subcategoria <strong>'"
                        + sub.getNome().toUpperCase() + "'</strong> pois o Grupo <strong>'"
                        + sub.getCategoria().getNome().toUpperCase() + "</strong>' ao qual ela pertence está desabilitado.");
            }
        }

        if (sub.isAtivo() == ativo) {
            return;
        }

        sub.setAtivo(ativo);
        sub.setInativadoPorCascata(false); // Ação explícita do administrador limpa flag de cascata

        subcategoriaRepository.save(sub);

        if (!ativo) {
            // Desabilitar tipos ativos vinculados
            List<TipoServico> tiposAtivos = tipoServicoRepository.findBySubcategoriaIdAndAtivoTrue(id);
            for (TipoServico tipo : tiposAtivos) {
                tipo.setAtivo(false);
                tipo.setInativadoPorCascata(true);
                tipoServicoRepository.save(tipo);
            }
        } else {
            // Reabilitar apenas tipos inativados por cascata
            List<TipoServico> tiposParaRestaurar =
                    tipoServicoRepository.findBySubcategoriaIdAndInativadoPorCascataTrue(id);
            for (TipoServico tipo : tiposParaRestaurar) {
                tipo.setAtivo(true);
                tipo.setInativadoPorCascata(false);
                tipoServicoRepository.save(tipo);
            }
        }
    }

    /**
     * ALTERAR O STATUS (ATIVO/INATIVO) DE UM TIPO DE SERVIÇO
    */
    @Transactional
    public void alterarStatusTipoServico(Integer id, boolean ativo) {
        TipoServico tipo = tipoServicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de serviço não encontrado com o ID: " + id));

        if (ativo) {
            if (!tipo.getSubcategoria().isAtivo()) {
                throw new BusinessException("Não é possível habilitar a Subcategoria <strong>'"
                        + tipo.getNome().toUpperCase() + "'</strong> pois a Categoria <strong>'"
                        + tipo.getSubcategoria().getNome().toUpperCase() + "'</strong> a qual ela pertence está desabilitada.");
            }
            if (!tipo.getCategoria().isAtivo()) {
                throw new BusinessException("Não é possível habilitar a Subcategoria <strong>'"
                        + tipo.getNome().toUpperCase() + "'</strong> pois a Categoria <strong>'"
                        + tipo.getSubcategoria().getNome().toUpperCase() + "'</strong> a qual ela pertence está desabilitada.");
            }
        }

        tipo.setAtivo(ativo);
        tipo.setInativadoPorCascata(false); // Ação manual explícit

        tipoServicoRepository.save(tipo);
    }

    /**
     * Mapeia a entidade Categoria o Record CategoriaDemandaResponseDTO
     */
    private CategoriaDemandaResponseDTO toCategoriaDTO(CategoriaDemanda cat) {
        return new CategoriaDemandaResponseDTO(
                cat.getId(),
                cat.getNome(),
                cat.getDescricao(),
                cat.isAtivo(),
                cat.getOrdemExibicao()
        );
    }

    /**
     * Mapeia a entidade Tipo Servico para o Record TipoServicoResponseDTO,
     * navegando pelo relacionamento com Subcategoria de forma segura.
     */
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
     * navegando pelo relacionamento com Categoria de forma segura.
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
