package br.gov.rn.natal.cadpgmapi.load_pdf.controllers;

import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.load_pdf.dtos.DocumentoResponseDTO;
import br.gov.rn.natal.cadpgmapi.load_pdf.services.DocumentoOrquestradorService;
import br.gov.rn.natal.cadpgmapi.load_pdf.services.DocumentoStorageService;
import br.gov.rn.natal.cadpgmapi.load_pdf.services.ServidorDocumentoService;
import br.gov.rn.natal.cadpgmapi.repository.ServidorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/servidores") // Ajuste para o caminho base real da sua API
@Tag(name = "Documentos", description = "API de Gestão de Arquivos PDF")
public class ServidorDocumentoController {

    private final ServidorDocumentoService documentoService;
    private final DocumentoOrquestradorService orquestradorService;

    // Construtor
    public ServidorDocumentoController(
            ServidorDocumentoService documentoService,
            DocumentoOrquestradorService orquestradorService) {
        this.documentoService = documentoService;
        this.orquestradorService = orquestradorService;
    }
    /**
     * 1. UPLOAD: Anexar um novo documento PDF ao servidor
     * POST /api/servidores/{servidorId}/documentos
     */
    @PostMapping(value = "/{servidorId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Faz upload de arquivos PDF vinculados a um servidor",
            description = "Envia os arquivos para o server MinIO")
    public ResponseEntity<String> uploadDocument(
            @PathVariable Integer servidorId,
            @RequestParam("files") List<MultipartFile> files) throws Exception {

        // Impede requisições sem arquivo ou com arquivo vazio (0 KB)
        if (files == null ||files.isEmpty()) {
            throw new BusinessException("Nenhum arquivo foi selecionado.");
        }
            // Envia para o Service (O mesmo método que você já tem pronto!)
        orquestradorService.processBatchUpload(servidorId, files);

        return ResponseEntity.status(HttpStatus.CREATED).body("Documentos anexado com sucesso.");
    }

    /**
     * 2. LISTAGEM: Retorna os metadados (DTO) de todos os PDFs de um servidor
     * GET /api/servidores/{servidorId}/documentos
     */
    @GetMapping("/{servidorId}/documents")
    @Operation(summary = "Buscar todos os arquivos PDF vinculados a um Servidor",
            description = "Retorna os links de todos os arquivos PDF para visualização")
    public ResponseEntity<List<DocumentoResponseDTO>> listDocuments(
            @PathVariable Integer servidorId) {

        List<DocumentoResponseDTO> documents = documentoService.listDocuments(servidorId);
        return ResponseEntity.ok(documents);
    }

    /**
     * 3. VISUALIZAÇÃO: Retorna o link seguro (Presigned URL) do MinIO para abrir o PDF
     * GET /api/servidores/documentos/{documentoId}/link
     */
    @GetMapping("/documents/{documentId}/link")
    @Operation(summary = "Gera links para os arquivos PDF",
            description = "Cria os links para os arquivos PDF para visualização")
    public ResponseEntity<String> generateViewLink(@PathVariable Integer documentId) throws Exception {

        String urlAcesso = documentoService.generateAccessLink(documentId);
        return ResponseEntity.ok(urlAcesso);
    }


     // 4. EXCLUSÃO DE UM ARQUIVO POR VEZ: Apaga o registro do MariaDB e o arquivo do MinIO
    @DeleteMapping("/documents/{documentId}")
    @Operation(summary = "Remove um arquivo PDF por vez vinculado ao Servidor",
            description = "Remove um arquivo PDF por vez")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Integer documentId) throws Exception {

        documentoService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping( "/documents/batch")
    @Operation(summary = "Remove lote com um ou mais arquivos PDF vinculados ao Servidor",
            description = "Remove lote com um o mais arquivos PDF")
    public ResponseEntity<Void>deleteDocumentsInBatch(@RequestBody List<Integer> documentsIds) {

        if (documentsIds == null ||documentsIds.isEmpty()) {
            throw new BusinessException("Nenhum documento foi selecionado para exclusão.");
        }

        orquestradorService.deleteDocumentsInBatch(documentsIds);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }
}
