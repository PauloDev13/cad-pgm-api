package br.gov.rn.natal.cadpgmapi.controller;

import br.gov.rn.natal.cadpgmapi.controller.generic.BaseController;
import br.gov.rn.natal.cadpgmapi.dto.request.ServidorRequestDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.AniversarianteResponseDTO;
import br.gov.rn.natal.cadpgmapi.dto.response.ServidorResponseDTO;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.load_pdf.services.DocumentoStorageService;
import br.gov.rn.natal.cadpgmapi.repository.ServidorRepository;
import br.gov.rn.natal.cadpgmapi.service.ServidorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final ServidorRepository servidorRepository;

    public ServidorController(ServidorService service, DocumentoStorageService storageService, ServidorRepository servidorRepository) {
        super(service);
        this.service = service;
        this.storageService = storageService;
        this.servidorRepository = servidorRepository;
    }

    // Ensina ao pai como extrair o ID para montar a URL do HTTP 201
    @Override
    protected Integer getIdFromDto(ServidorResponseDTO dto) {
        return dto.id();
    }

    //  Define que, se o usuário não mandar paginação, a lista vem ordenada por nome!
    @Override
    protected String getDefaultSortProperty() {
        return "nome";
    }

    // Mantemos APENAS o endpoint que é exclusivo desta entidade
    @GetMapping("/searchFilter")
    @Operation(summary = "Filtrar servidores por CPF, Matrícula, Nome Status, Cargo e Setor",
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

    @GetMapping("/aniversariantes")
    @Operation(summary = "Lista aniversariantes",
            description = "Retorna os servidores ativos que fazem aniversário no mês vigente")
    public ResponseEntity<List<AniversarianteResponseDTO>> getAniversariantes() {
        List<AniversarianteResponseDTO> lista = service.obterAniversariantesDoMesAtual();
        return ResponseEntity.ok(lista);
    }

    // Endpoint para a aba de excluídos. Lista todos os registros
    @GetMapping("/excluded")
    @Operation(summary = "Buscar todos os servidores com status de excluído",
            description = "Retorna os registros excluídos com 'Soft Delete'")
    public Page<ServidorResponseDTO> getExcluded(
            @ParameterObject @PageableDefault(
                    sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return service.listExcluded(pageable);
    }

    // Busca um servidor excluído pelo ID
    @GetMapping("/excluded/{id}")
    @Operation(summary = "Busca um servidor com status de excluído",
            description = "Retorna um registro excluído com 'Soft Delete'")
    public ServidorResponseDTO getExcludedById( @PathVariable Integer id) {
        return service.getExcludedById(id);
    }

    @GetMapping("/searchExcluded")
    @Operation(summary = "Buscar por Nome ou CPF servidores com status excluído",
            description = "Informe o Nome ou o CPF via query parameter. " +
                    "Exemplo: /searchExcluded?cpf=00011122233")
    public Page<ServidorResponseDTO> searchExcluded(
            @RequestParam(required = false) String term,
            @ParameterObject @PageableDefault(
                    sort = "nome", direction = Sort.Direction.ASC) Pageable pageable
    ) {

        return service.searchExcluded(term, pageable);
    }

    // Endpoint que o botão do Modal vai chamar para alterar o status de excluído
    // Usamos PATCH pois é uma alteração parcial/específica
    @PatchMapping("/{id}/reactivate")
    @Operation(summary = "Reativa o cadastro que está com status excluído",
            description = "Inverte o fluxo do 'Soft Delete'")
    public ResponseEntity<ServidorResponseDTO> reactivate(
            @PathVariable Integer id,
            @RequestBody ServidorRequestDTO dto) {
        return ResponseEntity.ok(service.reativated(id, dto));
    }

    @GetMapping(value = "/{servidorId}/photo")
    @Operation(summary = "Busca a foto de perfil do servidor",
            description = "Exibe a foto servidor no formulário de cadastro")
    public ResponseEntity<InputStreamResource> exibirFotoPerfil(@PathVariable Integer servidorId) throws Exception {

        // 1. Busca qual é o nome do arquivo lá no banco de dados (ex: "fotos/perfil-9.png")
        Servidor servidor = servidorRepository.findById(servidorId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));

        if (servidor.getPhotoPath() == null) {
            // Se não tiver foto, retorna um 404 limpo (o frontend trata mostrando uma foto cinza padrão)
            return ResponseEntity.notFound().build();
        }

        // 2. Puxa o fluxo do MinIO (o "túnel" que criamos no Passo 1)
        InputStream streamMinio = storageService.getDownloadStream(servidor.getPhotoPath());

        // 3. Descobre o Content-Type para avisar o navegador se é JPG ou PNG
        MediaType mediaType = servidor.getPhotoPath().endsWith(".png")
                ? MediaType.IMAGE_PNG
                : MediaType.IMAGE_JPEG;

        // 4. A MÁGICA DA ENTREGA (Streaming + Cache)
        return ResponseEntity.ok()
                // Diz pro navegador: "Guarde essa foto por 30 dias na sua memória"
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .contentType(mediaType)
                .body(new InputStreamResource(streamMinio));
    }

    @PostMapping(value = "/{servidorId}/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Faz o upload/atualização da foto de perfil do servidor",
            description = "Recebe a imagem, valida os Magic Numbers e salva no MinIO")
    public ResponseEntity<Void> uploadFotoPerfil(
            @PathVariable Integer servidorId,
            @RequestParam("file") MultipartFile file) throws Exception {

        // 1. Impedir o avanço se o arquivo vier nulo ou totalmente vazio
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Nenhum arquivo de imagem foi selecionado.");
        }

        // 🌟 AQUI ESTÁ O USO DO SEU SERVICE!
        // Agora o Controller chama o método que estava "isolado"
        service.uploadProfilePicture(servidorId, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }
}
