package br.gov.rn.natal.cadpgmapi.load_pdf.controllers;

import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.load_pdf.dtos.DocumentoResponseDTO;
import br.gov.rn.natal.cadpgmapi.load_pdf.services.DocumentoOrquestradorService;
import br.gov.rn.natal.cadpgmapi.load_pdf.services.ServidorDocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/servidores")
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
     * POST /api/v1/servidores/{servidorId}/documents
     */
    @PostMapping(value = "/{servidorId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Faz upload de arquivos PDF vinculados a um servidor",
            description = "Envia os arquivos para o servidor MinIO")
    public ResponseEntity<String> uploadDocument(
            @PathVariable Integer servidorId,
            @RequestParam("files") List<MultipartFile> files) throws IOException {

        // Impede requisições sem arquivo ou com arquivo vazio (0 KB)
        if (files == null || files.isEmpty()) {
            throw new BusinessException("Nenhum arquivo foi selecionado.");
        }

        // Delega o processamento (validação + salvamento) ao orquestrador
        orquestradorService.processBatchUpload(servidorId, files);

        return ResponseEntity.status(HttpStatus.CREATED).body("Documentos anexados com sucesso.");
    }

    /**
     * 2. LISTAGEM: Retorna os metadados (DTO) de todos os PDFs de um servidor
     * GET /api/v1/servidores/{servidorId}/documents
     */
    @GetMapping("/{servidorId}/documents")
    @Operation(summary = "Buscar todos os arquivos PDF vinculados a um Servidor",
            description = "Retorna os metadados de todos os arquivos PDF para visualização")
    public ResponseEntity<List<DocumentoResponseDTO>> listDocuments(
            @PathVariable Integer servidorId) {

        List<DocumentoResponseDTO> documents = documentoService.listDocuments(servidorId);
        return ResponseEntity.ok(documents);
    }

    /**
     * 3. VISUALIZAÇÃO: Retorna o link seguro (Presigned URL) do MinIO para abrir o PDF
     * GET /api/v1/servidores/documents/{documentId}/link
     */
    @GetMapping("/documents/{documentId}/link")
    @Operation(summary = "Gera links para os arquivos PDF",
            description = "Cria os links pré-assinados para os arquivos PDF")
    public ResponseEntity<String> generateViewLink(@PathVariable Integer documentId) throws IOException {

        String urlAcesso = documentoService.generateAccessLink(documentId);
        return ResponseEntity.ok(urlAcesso);
    }

    /**
     * 4. EXCLUSÃO DE UM ARQUIVO POR VEZ: Apaga o registro do banco e o arquivo do MinIO
     * DELETE /api/v1/servidores/documents/{documentId}
     */
    @DeleteMapping("/documents/{documentId}")
    @Operation(summary = "Remove um arquivo PDF por vez vinculado ao Servidor",
            description = "Remove um arquivo PDF por vez")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Integer documentId) throws IOException {

        documentoService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 5. EXCLUSÃO EM LOTE: Apaga os registros e os arquivos do MinIO
     * DELETE /api/v1/servidores/documents/batch
     */
    @DeleteMapping("/documents/batch")
    @Operation(summary = "Remove lote com um ou mais arquivos PDF vinculados ao Servidor",
            description = "Remove lote com um ou mais arquivos PDF")
    public ResponseEntity<Void> deleteDocumentsInBatch(@RequestBody List<Integer> documentsIds) {

        if (documentsIds == null || documentsIds.isEmpty()) {
            throw new BusinessException("Nenhum documento foi selecionado para exclusão.");
        }

        orquestradorService.deleteDocumentsInBatch(documentsIds);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}