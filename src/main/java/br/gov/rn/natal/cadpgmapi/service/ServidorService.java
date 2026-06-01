package br.gov.rn.natal.cadpgmapi.service;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.audit.utils.AuditDiffUtil;
import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.*;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.load_pdf.services.DocumentoStorageService;
import br.gov.rn.natal.cadpgmapi.mapper.ServidorMapper;
import br.gov.rn.natal.cadpgmapi.models.ServidorShadowProjection;
import br.gov.rn.natal.cadpgmapi.repository.*;
import br.gov.rn.natal.cadpgmapi.service.generic.BaseGenericService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Optional;
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
            EntityManager entityManager
            ){
        super(repository, mapper);
        this.servidorRepository = repository;
        this.sistemaRepository = sistemaRepository;
        this.aliasRepository = aliasRepository;
        this.procuradorRepository = procuradorRepository;
        this.setorRepository = setorRepository;
        this.entityManager = entityManager;
        this.storageService = storageService;
    }

    /* ============================================
        MÉTODOS GET
    * =============================================*/
    // Busca paginada com filtros dinâmicos para registros de Servidores ATIVOS
    @Transactional(readOnly = true)
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

    // Busca os Servidores ATIVOS e aniversarianates do mês atual do sistema
    @Transactional(readOnly = true)
    public List<AniversarianteResponseDTO> obterAniversariantesPorMes(Integer month) {
        return servidorRepository.findAniversariantesDoMes(month);
    }

    // Busc a servidores Ativos para emissão da folha de ponto
    @Transactional(readOnly = true)
    public List<FolhaPontoSetorResponseDTO> obterFolhaDePontoGeral() {
        // 1. Busca todos os dados do banco (rápidos, planos e ordenados)
        List<FolhaPontoProjectionDTO> projecoes = servidorRepository.findAllDadosFolhaPonto();

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


    // Busca todos os registros dos Sevidores DESLIGADOS
    @Transactional(readOnly = true)
    public Page<ServidorResponseDTO> listExcluded(Pageable pageable) {
        return servidorRepository.findAllExcluded(pageable).map(mapper::toDto);
    }

    // Retorna a String do caminho (ex: "fotos/perfil-9.png") ou nulo se não achar
    @Transactional(readOnly = true)
    public String getPhotoPathById(Integer id) {
        return servidorRepository.findPhotoPathByIdIgnoreStatus(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));

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

    // Método que "reativa" registros de um Servidor DESLIGADO para ATIVOS
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entity = "Servidor")
    public ServidorResponseDTO reativated(Integer id, ServidorRequestDTO dto) {
        // A. Primeiro, usamos o "Raio-X" para garantir que o registro existe
        Optional<ServidorShadowProjection> shadow = servidorRepository.checkCpfStatus(dto.cpf().trim());
        if (shadow.isEmpty() || !shadow.get().getExcluded()) {
            throw  new ResourceNotFoundException("Servidor não encontrado na base de dados de excluídos");
        }

        // B. Ressuscita no banco via SQL Nativo (Limpa a flag e a data)
        servidorRepository.reviveNativeServidor(id);

        // C. Carrega os dados do Servidor
        Servidor servidor = servidorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Falha ao recuperar servidor na reativação"));

        // D. Hidratação para garantir que os nomes dos cargos/setores venham preenchidos
        entityManager.refresh(servidor);

        // E. Tira a "foto" antiga (Ele já não está 'excluído', mas Cargo/Setor/Email ainda são os antigos)
        ServidorResponseDTO oldSnapshot = mapper.toDto(servidor);

        // F. Aplica os novos dados vindos do Modal (Novo cargo, novo setor, etc.)
        mapper.updateEntityFromDTO(servidor, dto);
        associarRelacoesMuitosParaMuitos(servidor, dto);

        servidorRepository.saveAndFlush(servidor);
        entityManager.refresh(servidor);

        // G. Tira a "foto" nova com os dados atualizados
        ServidorResponseDTO newSnapshot = mapper.toDto(servidor);

        // H. Usa a nossa classe utilitária universal!
        String diff = AuditDiffUtil.generateDiff(oldSnapshot, newSnapshot);

        if (diff != null && !diff.trim().isEmpty()) {
            AuditContextHolder.setLogDetalhes("READMISSÃO: Alterações:" + diff);
        } else {
            AuditContextHolder.setLogDetalhes("READMISSÃO: Nenhuma alteração de dados detectada.");
        }

        return newSnapshot;

    }

    // Realiza o upload de foto para o cadastro do Servidor
    @Transactional
    @Auditable(action = AuditAction.UPDATE, entity = "Servidor")
    public void uploadProfilePicture(
            Integer servidorId,
            MultipartFile file
    ) throws Exception {
        // 1. Valida se o servidor existe
        Servidor servidor = servidorRepository.findById(servidorId)
                .orElseGet(() -> servidorRepository.getExcludedById(servidorId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado para o ID informado.")));

        // Isso garante que o log dirá exatamente de quem é a foto que foi alterada
        AuditContextHolder.setEntityName("Servidor");
        AuditContextHolder.setFriendlyId(servidor.getNome()); // ou getNomeCompleto(), conforme sua entidade
        AuditContextHolder.setLogDetalhes("Inclusão/Atualização da foto de perfil do servidor: "
                + servidor.getNome());

        // 2. Valida os Magic Numbers (Segurança!)
        validatePhotoFormat(file);

        // 3. Lógica do MinIO
        String extensao = file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf("."));

        String namePhtoMinio = "fotos/perfil-" + servidorId + extensao;

        // 4. Salva no MinIO (Usando o método de upload que vocês já têm)
        storageService.upload(file, namePhtoMinio);

        // 5. Salva o caminho da foto no banco de dados, na tabela do Servidor
        servidor.setPhotoPath(namePhtoMinio);
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
                    "Este CPF (<strong>" + cpfFormat(dto.cpf()) + "</strong>)</br>já está em uso em outro cadastro."
            );
        }

        // 2. Validação da Matrícula (Bloqueia se existir a mesma Matrícula em ATIVO OU EXCLUÍDO)
        Optional<ServidorShadowProjection> shadowMatricula = servidorRepository.checkMatriculaStatus(dto.matricula().trim());
        if (shadowMatricula.isPresent()) {
            throw new BusinessException(
                    "Esta Matrícula (<strong>" + dto.matricula() + "</strong>)</br>" +
                            "já está em uso em outro cadastro."
            );
        }

        // 3. Validação de E-mail Pessoal
        servidorRepository.checkEmailPessoalStatus(dto.emailPessoal().trim())
                .ifPresent(s -> {
                    throw new BusinessException("Este E-mail (<strong>" + dto.emailPessoal() + "</strong>)</br>" +
                            "já está em uso em outro cadastro.");
                });

        // 4. Validação de E-mail Institucional (Só valida se for informado)
        if (dto.emailInstitucional() != null && !dto.emailInstitucional().isBlank()) {
            servidorRepository.checkEmailInstitucionalStatus(dto.emailInstitucional().trim())
                    .ifPresent(s -> {
                        throw new BusinessException("Este E-mail (<strong>" + dto.emailInstitucional() + "</strong>)</br>" +
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
                        "Este CPF (<strong>" + cpfFormat(dto.cpf()) + "</strong>)</br>já está em uso em outro cadastro."
                );
            }
        }

        // 2. Só valida se MUDOU a Matrícula na tela
        if (!existingServidor.getMatricula().equalsIgnoreCase(dto.matricula().trim())) {
            Optional<ServidorShadowProjection> shadowMatricula = servidorRepository.checkMatriculaStatus(dto.matricula().trim());
            if (shadowMatricula.isPresent()) {
                throw new BusinessException(
                        "Esta Matrícula (<strong>" + dto.matricula() + "</strong>)</br>já está em uso em outro cadastro."
                );
            }
        }

        // Validação de E-mail Pessoal
        if (!existingServidor.getEmailPessoal().equalsIgnoreCase(dto.emailPessoal().trim())) {
            servidorRepository.checkEmailPessoalStatus(dto.emailPessoal().trim())
                    .ifPresent(s -> {
                        throw new BusinessException("Este E-mail (<strong>" + dto.emailPessoal() + "</strong>)</br>" +
                                "já está em uso em outro cadastro."
                        );
                    });
        }

        // Validação de E-mail Institucional
        String newEmailInst = dto.emailInstitucional() != null ? dto.emailInstitucional().trim() : "";
        String oldEmailInst = existingServidor.getEmailInstitucional() != null
                ? existingServidor.getEmailInstitucional() : "";

        if (!newEmailInst.isBlank() && !newEmailInst.equalsIgnoreCase(oldEmailInst)) {
            servidorRepository.checkEmailInstitucionalStatus(newEmailInst)
                    .ifPresent(s -> {
                        throw new BusinessException("Este E-mail (<strong>" + newEmailInst + "</strong>)</br>" +
                                "já está em uso em outro cadastro.");
                    });
        }

        // 1. Tira a foto do dado antigo antes de ser alterado
        ServidorResponseDTO oldSnapshot = mapper.toDto(existingServidor);

        // 2. Guarda temporariamente para usar no afterSave (usando o contexto da thread)
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
        // Programação Defensiva: Verifica se o servidor tem um status associado para evitar NullPointerException
        if (entity.getStatus().equals(null)) {
            throw new BusinessException("Não é possível excluir um servidor sem status definido");
        }
        // Compara a descrição ignorando maiúsculas e minúsculas
        if (!entity.getStatus().getDescricao().equalsIgnoreCase("Inativo")) {
            throw new BusinessException("Somente Servidor com Status (<strong>'INATIVO'</strong>) pode ser removido." +
                    "Status atual: (<strong>'" + entity.getStatus().getDescricao().toUpperCase() + "'</strong>)."
            );
        }

    }

    /* ============================================
        MÉTODOS AUXILIARES PRIVADOS
    * =============================================*/
    /**
     * Recebe a entidade (já mapeada com os dados básicos pelo MapStruct)
     * e os IDs vindos do DTO para fazer a associação otimizada.
     */
    private void associarRelacoesMuitosParaMuitos(Servidor entity, ServidorRequestDTO dto) {

        // Associa Sistemas
        if (dto.sistemaIds() != null && !dto.sistemaIds().isEmpty()) {

            // Cria uma lista caso ela seja nula ou limpa a lista se ela existir
            if (entity.getSistemas() == null) {
                entity.setSistemas(new HashSet<>());
            } else {
                entity.getSistemas().clear();
            }

            // Adiciona os IDs à tabela de junção
            dto.sistemaIds().forEach(id -> {
                entity.getSistemas().add(sistemaRepository.getReferenceById(id));
            });
        }

        // Associa Aliases de E-mail
        if (dto.aliasIds() != null && !dto.aliasIds().isEmpty()) {
            if (entity.getAliases() == null) {
                entity.setAliases(new HashSet<>());
            } else {
                entity.getAliases().clear();
            }

            dto.aliasIds().forEach(id -> {
                entity.getAliases().add(aliasRepository.getReferenceById(id));
            });
        }

        // Associa Procuradores
        if (dto.procuradorIds() != null && !dto.procuradorIds().isEmpty()) {
            if (entity.getProcuradores() == null) {
                entity.setProcuradores(new HashSet<>());
            } else {
                entity.getProcuradores().clear();
            }

            dto.procuradorIds().forEach(id -> {
                entity.getProcuradores().add(procuradorRepository.getReferenceById(id));
            });
        }
    }

    // Valida o formato das fotos
    private void validatePhotoFormat(MultipartFile file) throws Exception {
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

        if (!isPng && !isJpeg) {
            throw new BusinessException("Formato inválido. Envie apenas fotos JPG, JPEG ou PNG.");
        }
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
