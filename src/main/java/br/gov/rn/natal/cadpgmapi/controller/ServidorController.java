package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.controller.generic.BaseController;
import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.AniversarianteResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.FolhaPontoSetorResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ProcuradorVinculoResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.load_pdf.services.DocumentoStorageService;
import br.gov.rn.natal.cadpgmapi.service.ServidorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/servidores")
@Tag(name = "Servidores", description = "API de Gestão de Servidores")
public class ServidorController extends BaseController<
        Servidor, ServidorRequestDTO, ServidorResponseDTO, Integer> {

    private final ServidorService service;
    private final DocumentoStorageService storageService;

    // DEFICIÊNCIA CORRIGIDA (b4.15/b5.3): o construtor recebia o ServidorService DUAS VEZES
    // (campos 'service' e 'servidorService' apontando para a MESMA instância), o que confundia
    // a leitura. Agora há apenas uma referência única.
    public ServidorController(
            ServidorService service,
            DocumentoStorageService storageService) {
        super(service);
        this.service = service;
        this.storageService = storageService;
    }

    // Ensina ao pai como extrair o ID para montar a URL do HTTP 201
    @Override
    protected Integer getIdFromDto(ServidorResponseDTO dto) {
        return dto.id();
    }

    // Define que, se o usuário não mandar paginação, a lista vem ordenada por nome!
    @Override
    protected String getDefaultSortProperty() {
        return "nome";
    }

    /* ==================================
     ENDPOINTS PARA STATUS ATIVOS
    *==================================== */

    // Busca paginada de todos os Servidores com status ATIVO
    @GetMapping("/searchFilter")
    @Operation(summary = "Filtrar servidores por CPF, Matrícula, Nome, Status, Cargo e Setor",
            description = "Informe a combinação de filtros para realizar a pesquisa.")
    public Page<ServidorResponseDTO> findByFilters(
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String matricula,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Integer cargoId,
            @RequestParam(required = false) Integer setorId,
            @ParameterObject @PageableDefault(
                    sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        return service.findByFilters(cpf, matricula, nome, statusId, cargoId, setorId, pageable);
    }

    // Busca os aniversariantes do mês entre os Servidores ATIVOS
    @GetMapping("/aniversariantes")
    @Operation(summary = "Lista aniversariantes",
            description = "Retorna os servidores ativos que fazem aniversário no mês informado")
    public ResponseEntity<List<AniversarianteResponseDTO>> getAniversariantes(
            @RequestParam("month") @Min(1) @Max(12) Integer month
    ) {
        List<AniversarianteResponseDTO> lista = service.obterAniversariantesPorMes(month);
        return ResponseEntity.ok(lista);
    }

    // Busca registros para emitir a folha de ponto
    @GetMapping("/folha-ponto")
    @Operation(
            summary = "Gerar dados da Folha de Ponto por Setores",
            description = "Retorna os servidores ativos agrupados por setor. Permite filtrar por um ou múltiplos setores. " +
                    "Caso nenhum setor seja informado (ou lista vazia), retorna todos os setores."
    )
    public ResponseEntity<List<FolhaPontoSetorResponseDTO>> getDadosFolhaPonto(
            @RequestParam(name = "setorIds", required = false) List<Integer> setorIds
    ) {
        List<FolhaPontoSetorResponseDTO> folhaPonto = service.obterFolhaDePonto(setorIds);
        return ResponseEntity.ok(folhaPonto);
    }

    // retorna lista de procuradores com os servidores a eles vinculados (uso do certificado digitaç
    @Operation(
            summary = "Listar vínculos de servidores por procurador",
            description = "Retorna a listagem de servidores (com seus respectivos setores) vinculados a um determinado " +
                    "procurador pesquisado por nome."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de servidores vinculados retornada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping("/vinculos")
    public ResponseEntity<List<ProcuradorVinculoResponseDTO>> listarVinculosPorProcurador(
            @Parameter(
                    description = "Lista de nomes de procuradores para filtragem (opcional). Aceita múltiplos " +
                            "parâmetros ou valores separados por vírgula.",
                    example = "Carlos Eduardo, Maria Helena"
            )
            @RequestParam(name = "procuradores", required = false) List<String> procuradores
    ) {
        List<ProcuradorVinculoResponseDTO> response = service.listarVinculosAgrupadosPorProcurador(procuradores);
        return ResponseEntity.ok(response);
    }

    // Faz o upload de fotos para os Servidores com status ATIVO
    @PostMapping(value = "/{servidorId}/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Faz o upload/atualização da foto de perfil do servidor",
            description = "Recebe a imagem, valida os Magic Numbers e salva no MinIO")
    public ResponseEntity<Void> uploadFotoPerfil(
            @PathVariable Integer servidorId,
            @RequestParam("file") MultipartFile file) throws IOException {

        // 1. Impede o avanço se o arquivo vier nulo ou totalmente vazio
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Nenhum arquivo de imagem foi selecionado.");
        }

        service.uploadProfilePicture(servidorId, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    /* ==================================
     ENDPOINTS PARA STATUS DESLIGADOS
    *==================================== */

    // Busca paginada de todos os Servidores com status DESLIGADO
    @GetMapping("/excluded")
    @Operation(summary = "Buscar todos os servidores com status de excluído",
            description = "Retorna os registros excluídos com 'Soft Delete'")
    public Page<ServidorResponseDTO> getExcluded(
            @ParameterObject @PageableDefault(
                    sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.listExcluded(pageable);
    }

    // Busca um Servidor com status DESLIGADO pelo ID
    @GetMapping("/excluded/{id}")
    @Operation(summary = "Busca um servidor com status de excluído",
            description = "Retorna um registro excluído com 'Soft Delete'")
    public ServidorResponseDTO getExcludedById(@PathVariable Integer id) {
        return service.getExcludedById(id);
    }

    // Aplica filtragem paginada nos Servidores com status DESLIGADOS
    @GetMapping("/searchExcluded")
    @Operation(summary = "Buscar por Nome ou CPF servidores com status excluído",
            description = "Informe o Nome ou o CPF via query parameter. " +
                    "Exemplo: /searchExcluded?term=maria")
    public Page<ServidorResponseDTO> searchExcluded(
            @RequestParam(required = false) String term,
            @ParameterObject @PageableDefault(
                    sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        return service.searchExcluded(term, pageable);
    }

    // Muda o status de um Servidor de DESLIGADO para ATIVO (READMISSÃO)
    // Usamos PATCH, pois é uma alteração parcial/específica
    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Reativa o cadastro que está com status excluído",
            description = "Inverte o fluxo do 'Soft Delete'")
    public ResponseEntity<ServidorResponseDTO> reactivate(
            @PathVariable Integer id,
            @RequestBody ServidorRequestDTO dto) {
        return ResponseEntity.ok(service.reactivate(id, dto));
    }

    // Busca a foto do Servidor ATIVO OU DESLIGADO
    @GetMapping(value = "/{servidorId}/photo")
    @Operation(summary = "Busca a foto de perfil do Servidor ativo ou desligado",
            description = "Exibe a foto do Servidor ativo ou desligado no formulário de cadastro")
    public ResponseEntity<InputStreamResource> exibirFotoPerfil(
            @PathVariable Integer servidorId
    ) throws IOException {

        // Retorna o caminho da foto gravada no BD
        String photoPath = service.getPhotoPathById(servidorId);

        if (photoPath == null) {
            // Se não tiver foto, retorna um 404 limpo (o frontend trata mostrando uma foto cinza padrão)
            return ResponseEntity.notFound().build();
        }

        // Puxa o fluxo do MinIO (o "túnel")
        InputStream streamMinio = storageService.getDownloadStream(photoPath);

        // Descobre o Content-Type para avisar o navegador se é JPG ou PNG
        MediaType mediaType = photoPath.endsWith(".png")
                ? MediaType.IMAGE_PNG
                : MediaType.IMAGE_JPEG;

        // (Streaming + Cache por 30 dias)
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .contentType(mediaType)
                .body(new InputStreamResource(streamMinio));
    }
}