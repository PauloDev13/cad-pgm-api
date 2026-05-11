package br.gov.rn.natal.cadpgmapi.load_pdf.controllers;

import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.load_pdf.dtos.DocumentoResponseDTO;
import br.gov.rn.natal.cadpgmapi.load_pdf.services.ServidorDocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/servidores") // Ajuste para o caminho base real da sua API
@Tag(name = "Documentos", description = "API de Gestão de Arquivos PDF")
public class ServidorDocumentoController {

    private final ServidorDocumentoService documentoService;

    // Construtor
    public ServidorDocumentoController(ServidorDocumentoService documentoService) {
        this.documentoService = documentoService;
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
            @RequestParam("file") MultipartFile file) throws Exception {

        // 1. Nova Validação: Impede requisições sem arquivo ou com arquivo vazio (0 KB)
        if (file == null ||file.isEmpty()) {
            throw new BusinessException("Nenhum arquivo foi selecionado ou o arquivo está vazio.");
        }

        // 2. Validação de Segurança: Garante que apenas PDFs reais sejam processados
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            // Usando a BusinessException que já temos no projeto!
            throw new BusinessException("Apenas arquivos no formato PDF são permitidos.");
        }

        documentoService.anexarDocumento(servidorId, file);

        return ResponseEntity.status(HttpStatus.CREATED).body("Documento anexado com sucesso.");
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

        List<DocumentoResponseDTO> documentos = documentoService.listDocuments(servidorId);
        return ResponseEntity.ok(documentos);
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

    /**
     * 4. EXCLUSÃO: Apaga o registro do MariaDB e o arquivo do MinIO
     * DELETE /api/servidores/documentos/{documentoId}
     */
    @DeleteMapping("/documents/{documentId}")
    @Operation(summary = "Remover arquivos PDF vinculados a um Servidor",
            description = "Remove os arquivos PDF")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Integer documentId) throws Exception {

        documentoService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}
