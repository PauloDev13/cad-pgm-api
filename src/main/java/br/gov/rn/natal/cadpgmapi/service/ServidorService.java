package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.audit.utils.AuditDiffUtil;
import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.AniversarianteResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.FolhaPontoProjectionDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.FolhaPontoServidorDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.FolhaPontoSetorResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.entity.Status;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.load_pdf.services.DocumentoStorageService;
import br.gov.rn.natal.cadpgmapi.mapper.ServidorMapper;
import br.gov.rn.natal.cadpgmapi.models.ServidorShadowProjection;
import br.gov.rn.natal.cadpgmapi.repository.AliasRepository;
import br.gov.rn.natal.cadpgmapi.repository.ProcuradorRepository;
import br.gov.rn.natal.cadpgmapi.repository.ServidorRepository;
import br.gov.rn.natal.cadpgmapi.repository.SetorRepository;
import br.gov.rn.natal.cadpgmapi.repository.SistemaRepository;
import br.gov.rn.natal.cadpgmapi.repository.StatusRepository;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import br.gov.rn.natal.cadpgmapi.utils.EntityChangeEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ServidorService extends BaseGenericService<
        Servidor, ServidorRequestDTO, ServidorResponseDTO, Integer> {
    // Variáveis
    private final ServidorRepository servidorRepository;
    private final SistemaRepository sistemaRepository;
    private final AliasRepository aliasRepository;
    private final ProcuradorRepository procuradorRepository;
    private final DocumentoStorageService storageService;
    private final SetorRepository setorRepository;
    private final StatusRepository statusRepository;
    private final EntityManager entityManager;

    // Construtor
    public ServidorService(
            ServidorRepository repository,
            ServidorMapper mapper,
            SistemaRepository sistemaRepository,
            AliasRepository aliasRepository,
            ProcuradorRepository procuradorRepository,
            DocumentoStorageService storageService,
            SetorRepository setorRepository,
            EntityManager entityManager,
            ApplicationEventPublisher eventPublisher, StatusRepository statusRepository
    ){
        super(repository, mapper, eventPublisher);
        this.servidorRepository = repository;
        this.sistemaRepository = sistemaRepository;
        this.aliasRepository = aliasRepository;
        this.procuradorRepository = procuradorRepository;
        this.setorRepository = setorRepository;
        this.statusRepository = statusRepository;
        this.entityManager = entityManager;
        this.storageService = storageService;
    }

    /*================================================================
                        MÉTODOS SOBRESCRITOS
      ================================================================ */

    // Sobrescreve o método para ensinar a classe mãe a deletar sem limpar
    // as tabelas com relacionamento N:N (servidor_sistema, servidor_alias, etc)
    @Override
    protected void performDelete(Servidor entity) {
        // (Ajuste pós-refatoração): o soft delete volta a marcar o servidor com o status
        // 'Inativo' (comportamento original). O status é resolvido PELO NOME na consulta
        // nativa do repositório, sem magic numbers acoplados à ordem do seed (V05).
        servidorRepository.softDeleteByID(entity.getId(), Status.STATUS_INATIVO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServidorResponseDTO> findAllSelect() {
        // Bloqueia os registros com excluded = false. Eles não vão na lista
        Specification<Servidor> spec = (root, query, cb) ->
                cb.isFalse(root.get("excluded"));
        return mapper.toDtoList(servidorRepository.findAll(spec));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServidorResponseDTO> findAll(Pageable pageable) {
        // Envia uma instrução direta pro banco: "WHERE excluded = false"
        Specification<Servidor> spec = (root, query, cb) ->
                cb.isFalse(root.get("excluded"));
        return servidorRepository.findAll(spec, pageable).map(mapper::toDto);
    }

    /*==========================================
                    MÉTODOS GET
      ==========================================*/
    // Busca paginada com filtros dinâmicos para registros de Servidores ATIVOS.
    // DEFICIÊNCIA CORRIGIDA (cache): sem a 'key', o Spring usava a chave vazia (SimpleKey.EMPTY) e
    // TODAS as combinações de filtro/paginação retornavam o MESMO resultado da primeira chamada.
    // Agora cada combinação de parâmetros tem sua própria entrada no cache.
    @Transactional(readOnly = true)
    @Cacheable(value = "servidoresCache", key = "{#cpf, #matricula, #nome, #statusId, #cargoId, #setorId, #pageable}")
    public Page<ServidorResponseDTO> findByFilters(
            String cpf,
            String matricula,
            String nome,
            Integer statusId,
            Integer cargoId,
            Integer setorId,
            Pageable pageable
    ) {
        // BLOCO DA SPECIFICATION: Monta as regras (a "receita" do SQL)
        Specification<Servidor> spec = (root, query, cb) -> {
            // Começa neutro (1=1)
            Predicate predicate = cb.conjunction();

            // Bloqueia os registros com excluded = true. Eles não vão na lista
            predicate = cb.and(predicate, cb.isFalse(root.get("excluded")));

            // Se o CPF for imformado, monta o SQL de busca por CPF
            if (cpf != null && !cpf.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.like(
                        root.get("cpf"), "%" + cpf.trim() + "%")
                );
            }

            // Se a Matrícula for imformada, monta o SQL de busca por matrícula
            if (matricula != null && !matricula.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.like(
                        cb.lower(root.get("matricula")), "%" + matricula.trim().toLowerCase() + "%")
                );
            }

            if (nome!= null && !nome.trim().isEmpty()) {
                predicate = cb.and(
                        predicate, cb.like(
                        cb.lower(root.get("nome")), "%" + nome.trim().toLowerCase() + "%")
                );

            }

            // Se o ID de Status, Cargo e Setor forem imformados,
            // monta o SQL de busca pelo ID de cada um combinados
            if (statusId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status").get("id"), statusId));
            }

            if (cargoId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("cargo").get("id"), cargoId));
            }

            if (setorId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("setor").get("id"), setorId));
            }

            // Encerra a montagem das regras
            return predicate;
        };

        // BLOCO DE EXECUÇÃO: Vai no banco e converte para DTO
        // O retorno real que vai para o Controller
        return servidorRepository.findAll(spec, pageable)
                .map(mapper::toDto);
    }

    // Busca os Servidores ATIVOS e aniversariantes do mês atual do sistema
    @Transactional(readOnly = true)
    public List<AniversarianteResponseDTO> obterAniversariantesPorMes(Integer month) {
        return servidorRepository.findAniversariantesDoMes(month, Status.STATUS_ATIVO);
    }

    // Busca servidores Ativos para emissão da folha de ponto
    @Transactional(readOnly = true)
    public List<FolhaPontoSetorResponseDTO> obterFolhaDePontoGeral() {
        // 1. Busca todos os dados do banco (rápidos, planos e ordenados)
        List<FolhaPontoProjectionDTO> projecoes = servidorRepository.findAllDadosFolhaPonto(Status.STATUS_ATIVO);

        // 2. Agrupa por nome do setor mantendo a ordem alfabética do SQL (LinkedHashMap)
        Map<String, List<FolhaPontoProjectionDTO>> agrupadoPorSetor = projecoes.stream()
                .collect(Collectors.groupingBy(
                        FolhaPontoProjectionDTO::nomeSetor,
                        LinkedHashMap::new, // Garante que a ordem não será bagunçada!
                        Collectors.toList()
                ));

        // 3. Converte o Map estruturado para a lista de DTOs final do Frontend
        return agrupadoPorSetor.entrySet().stream()
                .map(entry -> {
                    String nomeSetor = entry.getKey();

                    // Transforma a projection no DTO interno
                    List<FolhaPontoServidorDTO> servidores = entry.getValue().stream()
                            .map(p -> new FolhaPontoServidorDTO(
                                    p.nomeServidor(),
                                    p.vinculo(),
                                    p.tipoAtividade()
                            ))
                            .collect(Collectors.toList());

                    // Monta o nó principal (Nome, Total, Lista)
                    return new FolhaPontoSetorResponseDTO(
                            nomeSetor,
                            servidores.size(),
                            servidores
                    );
                })
                .collect(Collectors.toList());
    }


    // Busca todos os registros dos Servidores DESLIGADOS
    @Transactional(readOnly = true)
    public Page<ServidorResponseDTO> listExcluded(Pageable pageable) {
        return servidorRepository.findAllExcluded(pageable).map(mapper::toDto);
    }

    // Retorna a String do caminho (ex: "fotos/perfil-9.png") ou nulo se não achar
    @Transactional(readOnly = true)
    public String getPhotoPathById(Integer id) {
        return servidorRepository.findPhotoPathByIdIgnoreStatus(id)
                .orElseThrow(() -> new ResourceNotFoundException("Não há Foto cadastrada para o ID " + id));
    }

    // Busca um Servidor DESLIGADO por ID
    @Transactional(readOnly = true)
    public ServidorResponseDTO getExcludedById(Integer id) {
        Servidor servidor = servidorRepository.getExcludedById(id).orElseThrow(
                () -> new ResourceNotFoundException("Servidor não encontrado para o ID informado."));
        return  mapper.toDto(servidor);
    }

    // Busca paginada com filtros dinâmicos para registros DESLIGADOS
    @Transactional(readOnly = true)
    public Page<ServidorResponseDTO> searchExcluded(
            String term, Pageable pageable
    ) {
        if (term == null || term.trim().isEmpty()) {
            return listExcluded(pageable);
        }
        return servidorRepository.searchExcluded(term.trim(), pageable).map(mapper::toDto);
    }

    /* =====================================================
        MÉTODOS UPDATE
    * ======================================================*/

    // Método que "reativa" registros de um Servidor DESLIGADO para ATIVO
    @Transactional
    // Ativa a auditoria na entidade Servidor
    @Auditable(action = AuditAction.UPDATE, entity = "Servidor")
    public ServidorResponseDTO reactivate(Integer id, ServidorRequestDTO dto) {
        // A. Primeiro usamos o "Raio-X" para garantir que o registro (excluído) existe
        Optional<ServidorShadowProjection> shadow = servidorRepository.checkCpfStatus(dto.cpf().trim());
        if (shadow.isEmpty() || !shadow.get().getExcluded()) {
            throw new ResourceNotFoundException("Servidor não encontrado na base de dados de excluídos");
        }


        // B. Carrega os dados do Servidor
        Servidor servidor = servidorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Falha ao recuperar servidor na reativação"));

        // Chama gancho e valida se o CPF, Matricula e Email estão duplicados. Se sim
        // lança a exceção, o @Transactional cancela a ressurreição no banco (Rollback)
        // e bloqueia a ação de reativação
        beforeUpdate(dto, servidor);

        // C. Desfaz o Soft Delete no banco via SQL Nativo (Limpa a flag 'excluded' e o atributo 'excludeDate)
        servidorRepository.reviveNativeServidor(id);

        // D. Hidratação para garantir que os nomes dos cargos/setores venham preenchidos
        entityManager.refresh(servidor);

        // E. Tira o SNAPSHOT antigo (Ele já não está 'excluído', mas Cargo/Setor/Email ainda são os antigos)
        ServidorResponseDTO oldSnapshot = mapper.toDto(servidor);

        // F. Aplica os novos dados vindos do Modal (Novo cargo, novo setor, etc.)
        mapper.updateEntityFromDTO(servidor, dto);
        associarRelacoesMuitosParaMuitos(servidor, dto);

        // Após a reativação, o servidor volta à base ATIVA com o status 'Pendente'
        // (aguardando a equipe de RH revisar os dados)
        Status statusPendente = statusRepository.findByDescricaoIgnoreCase(Status.STATUS_PENDENTE)
                .orElseThrow(() -> new BusinessException("Status 'Pendente' não encontrado"));
        servidor.setStatus(statusPendente);

        // Salva com o status 'Pendente' e recarrega os dados completos
        servidorRepository.saveAndFlush(servidor);
        entityManager.refresh(servidor);

        // G. Tira o SNAPSHOT novo com os dados atualizados
        ServidorResponseDTO newSnapshot = mapper.toDto(servidor);

        // H. Usa a nossa classe utilitária universal!
        String diff = AuditDiffUtil.generateDiff(oldSnapshot, newSnapshot);

        if (diff != null && !diff.trim().isEmpty()) {
            AuditContextHolder.setLogDetalhes("READMISSÃO: Alterações:" + diff);
        } else {
            AuditContextHolder.setLogDetalhes("READMISSÃO: Nenhuma alteração de dados detectada.");
        }

        this.eventPublisher.publishEvent(new EntityChangeEvent(servidor));

        return newSnapshot;

    }

    // Realiza o upload de foto para o cadastro do Servidor
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entity = "Servidor")
    public void uploadProfilePicture(
            Integer servidorId,
            MultipartFile file
    ) throws IOException {
        // 1. Valida se o servidor existe (procurando também entre os DESLIGADOS)
        Servidor servidor = servidorRepository.findById(servidorId)
                .orElseGet(() -> servidorRepository.getExcludedById(servidorId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado para o ID informado.")));

        // Isso garante que o log dirá exatamente de quem é a foto que foi alterada
        AuditContextHolder.setEntityName("Servidor");
        AuditContextHolder.setFriendlyId(servidor.getNome());
        AuditContextHolder.setLogDetalhes("Inclusão/Atualização da foto de perfil do servidor: "
                + servidor.getNome());

        // 2. Valida os Magic Numbers e devolve o formato REAL detectado nos bytes ("png" ou "jpg").
        // ANTES: a extensão era extraída de file.getOriginalFilename() com
        // 'substring(lastIndexOf("."))' — lançava StringIndexOutOfBoundsException em arquivos
        // sem extensão e aceitava extensões forjadas (ex.: "foto.php"). Agora a extensão vem
        // dos próprios bytes do arquivo, que é a fonte confiável após validar a assinatura.
        String formatoImagem = detectImageFormat(file);

        // 3. Monta o caminho no MinIO usando o formato detectado, sem depender do nome do arquivo
        String photoObjectName = "fotos/perfil-" + servidorId + "." + formatoImagem;

        // 4. Salva no MinIO (Usando o método de upload que vocês já têm)
        storageService.upload(file, photoObjectName);

        // 5. Salva o caminho da foto no banco de dados, na tabela do Servidor
        servidor.setPhotoPath(photoObjectName);
        servidorRepository.save(servidor);
    }

    /* =====================================================
        MÉTODOS SOBRESCRITOS EXCLUSIVOS DE REGRA DE NEGÓCIO
    * ======================================================*/

    @Override
    protected void beforeCreate(ServidorRequestDTO dto) {
        // 1. Validação de CPF (Bloqueia se existir o mesmo CPF em ATIVO OU EXCLUÍDO)
        Optional<ServidorShadowProjection> shadowCpf = servidorRepository.checkCpfStatus(dto.cpf().trim());
        if (shadowCpf.isPresent()) {
            throw new BusinessException(
                    "Este CPF (<strong>" + cpfFormat(dto.cpf()) + "</strong>)<br/>já está em uso em outro cadastro."
            );
        }

        // 2. Validação da Matrícula (Bloqueia se existir a mesma Matrícula em ATIVO OU EXCLUÍDO)
        Optional<ServidorShadowProjection> shadowMatricula = servidorRepository.checkMatriculaStatus(dto.matricula().trim());
        if (shadowMatricula.isPresent()) {
            throw new BusinessException(
                    "Esta Matrícula (<strong>" + dto.matricula() + "</strong>)<br/>" +
                            "já está em uso em outro cadastro."
            );
        }

        // 3. Validação de E-mail Pessoal
        // DEFICIÊNCIA CORRIGIDA (NPE): o DTO permite emailPessoal nulo/em branco e a versão
        // anterior chamava dto.emailPessoal().trim() cegamente — NPE em tentativas inválidas.
        // Agora só consultamos o banco quando há um valor de fato informado.
        String emailPessoal = trimmedOrNull(dto.emailPessoal());
        if (emailPessoal != null) {
            servidorRepository.checkEmailPessoalStatus(emailPessoal)
                    .ifPresent(s -> {
                        throw new BusinessException("Este E-mail (<strong>" + emailPessoal + "</strong>)<br/>" +
                                "já está em uso em outro cadastro.");
                    });
        }

        // 4. Validação de E-mail Institucional (Só valida se for informado)
        if (dto.emailInstitucional() != null && !dto.emailInstitucional().isBlank()) {
            servidorRepository.checkEmailInstitucionalStatus(dto.emailInstitucional().trim())
                    .ifPresent(s -> {
                        throw new BusinessException("Este E-mail (<strong>" + dto.emailInstitucional() + "</strong>)<br/>" +
                                "já está em uso em outro cadastro.");
                    });
        }
    }

    @Override
    protected void beforeUpdate(ServidorRequestDTO dto, Servidor existingServidor) {
        // 1. Só valida se MUDOU o CPF na tela
        if (!existingServidor.getCpf().equalsIgnoreCase(dto.cpf().trim())) {
            Optional<ServidorShadowProjection> shadowCpf = servidorRepository.checkCpfStatus(dto.cpf().trim());

            if (shadowCpf.isPresent()) {
                throw new BusinessException(
                        "Este CPF (<strong>" + cpfFormat(dto.cpf()) + "</strong>)<br/>já está em uso em outro cadastro."
                );
            }
        }

        // 2. Só valida se MUDOU a Matrícula na tela
        if (!existingServidor.getMatricula().equalsIgnoreCase(dto.matricula().trim())) {
            Optional<ServidorShadowProjection> shadowMatricula = servidorRepository.checkMatriculaStatus(dto.matricula().trim());
            if (shadowMatricula.isPresent()) {
                throw new BusinessException(
                        "Esta Matrícula (<strong>" + dto.matricula() + "</strong>)<br/>já está em uso em outro cadastro."
                );
            }
        }

        // Validação de E-mail Pessoal
        // DEFICIÊNCIA CORRIGIDA (NPE): dto.emailPessoal() pode vir nulo; antes o .trim() lançava
        // NullPointerException. Agora, se não vier e-mail, a validação é simplesmente ignorada.
        String emailPessoalNovo = trimmedOrNull(dto.emailPessoal());
        if (emailPessoalNovo != null
                && !emailPessoalNovo.equalsIgnoreCase(existingServidor.getEmailPessoal())) {
            servidorRepository.checkEmailPessoalStatus(emailPessoalNovo)
                    .ifPresent(s -> {
                        throw new BusinessException("Este E-mail (<strong>" + emailPessoalNovo + "</strong>)<br/>" +
                                "já está em uso em outro cadastro."
                        );
                    });
        }

        // Validação de E-mail Institucional
        String newEmailInst = trimmedOrNull(dto.emailInstitucional());
        String oldEmailInst = existingServidor.getEmailInstitucional() != null
                ? existingServidor.getEmailInstitucional() : "";

        if (newEmailInst != null && !newEmailInst.equalsIgnoreCase(oldEmailInst)) {
            servidorRepository.checkEmailInstitucionalStatus(newEmailInst)
                    .ifPresent(s -> {
                        throw new BusinessException("Este E-mail (<strong>" + newEmailInst + "</strong>)<br/>" +
                                "já está em uso em outro cadastro.");
                    });
        }

        // (Ajuste pós-refatoração) O bloco que BLOQUEAVA o status 'Desligado' na atualização
        // foi removido: ele só fazia sentido enquanto o soft delete marcava o servidor como
        // 'Desligado'. Com o status do soft delete de volta para 'Inativo' (que é um status
        // comum do domínio, selecionável no formulário), não existe mais estado especial a proteger.

        // 5. Captura o snapshot (estado antigo) antes de aplicar as alterações
        ServidorResponseDTO oldSnapshot = mapper.toDto(existingServidor);

        // 6. Guarda o snapshot no contexto para o afterSave calcular o diff
        AuditContextHolder.setOldSnapshot(oldSnapshot);
    }

    @Override
    protected void afterSave(Servidor entity, ServidorRequestDTO dto) {

        // 3. Reassocia as coleções N:N
        associarRelacoesMuitosParaMuitos(entity,dto);

        // 4. Hidrata a entidade (traz nomes de Cargo, Setor, etc)
        entityManager.flush();
        entityManager.refresh(entity);

        // 5. Gera o Log de Comparação (Audit Diff)
        ServidorResponseDTO oldSnapshot = (ServidorResponseDTO) AuditContextHolder.getOldSnapshot();
        ServidorResponseDTO newSnapshot = mapper.toDto(entity);

        if (oldSnapshot != null) {
            String detailsLog = AuditDiffUtil.generateDiff(oldSnapshot, newSnapshot);
            AuditContextHolder.setLogDetalhes(detailsLog);
        }
    }

    @Override
    protected void beforeDelete(Servidor entity) {
        // CORREÇÃO DE BUG: antes era 'entity.getStatus().equals(null)':
        //  1) com status null, lançava NullPointerException em vez da regra de negócio;
        //  2) com status preenchido, '.equals(null)' sempre retorna false — a regra nunca disparava.
        // A forma correta de testar AUSÊNCIA de objeto é comparar a referência com '== null'.
        if (entity.getStatus() == null) {
            throw new BusinessException("Não é possível excluir um servidor sem status definido");
        }
    }

    /* ============================================
        MÉTODOS AUXILIARES PRIVADOS
    * =============================================*/
    /**
     * Recebe a entidade (já mapeada com os dados básicos pelo MapStruct)
     * e os IDs vindos do DTO para fazer a associação otimizada.
     * DEFICIÊNCIA DE LEGIBILIDADE (item 5.6): as três associações (Sistemas, Aliases e
     * Procuradores) repetiam o MESMO padrão "if null -> limpar -> forEach add". Extraímos
     * esse padrão para o helper genérico replaceCollectionAssociations e cada chamada abaixo
     * só informa qual repositório resolve o ID para a referência JPA.
     */
    private void associarRelacoesMuitosParaMuitos(Servidor entity, ServidorRequestDTO dto) {

        // Associa Sistemas
        replaceCollectionAssociations(
                entity.getSistemas(), dto.sistemaIds(), sistemaRepository::getReferenceById
        );

        // Associa Aliases de E-mail
        replaceCollectionAssociations(
                entity.getAliases(), dto.aliasIds(), aliasRepository::getReferenceById
        );

        // Associa Procuradores
        replaceCollectionAssociations(
                entity.getProcuradores(), dto.procuradorIds(), procuradorRepository::getReferenceById
        );
    }

    // Esvazia a coleção de referência e a repovoa a partir dos IDs do DTO.
    // O target nunca é nulo aqui: entidades gerenciadas pelo Hibernate inicializam as coleções
    // e as entidades recém-criadas usam o @Builder.Default da entidade Servidor.
    private <T> void replaceCollectionAssociations(
            Set<T> current, Set<Integer> ids, Function<Integer, T> referenceResolver
    ) {
        if (ids == null) {
            return;
        }
        current.clear();
        ids.forEach(id -> current.add(referenceResolver.apply(id)));
    }

    // Normaliza um texto vindo do DTO (ex.: e-mails opcionais), evitando NPE no .trim().
    private String trimmedOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    // Valida os Magic Numbers da imagem e devolve o formato real ("png" ou "jpg").
    // A versão anterior (validatePhotoFormat) apenas validava os bytes: a extensão usada no
    // caminho do MinIO continuava vinda do NOME do arquivo, o que causava
    // StringIndexOutOfBoundsException em arquivos sem extensão e aceitava extensões enganosas.
    private String detectImageFormat(MultipartFile file) throws IOException {
        // 1. Lemos os 4 primeiros bytes do arquivo
        byte[] header = new byte[4];
        try (InputStream is = file.getInputStream()) {
            is.read(header);
        }

        // 2. Assinatura do PNG: 89 50 4E 47
        boolean isPng = header[0] == (byte) 0x89 && header[1] == (byte) 0x50
                && header[2] == (byte) 0x4E && header[3] == (byte) 0x47;

        // 3. Assinatura do JPEG/JPG: FF D8 FF
        boolean isJpeg = header[0] == (byte) 0xFF && header[1] == (byte) 0xD8
                && header[2] == (byte) 0xFF;

        if (isPng) {
            return "png";
        }
        if (isJpeg) {
            return "jpg";
        }

        throw new BusinessException("Formato inválido. Envie apenas fotos JPG, JPEG ou PNG.");
    }

    // Método para formatar o CPF como 000.000.000-00
    private String cpfFormat(String cpf) {
        // return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        // Formatação direta, simples e extremamente rápida
        return cpf.substring(0, 3) + "." +
                cpf.substring(3, 6) + "." +
                cpf.substring(6, 9) + "-" +
                cpf.substring(9, 11);
    }
}
