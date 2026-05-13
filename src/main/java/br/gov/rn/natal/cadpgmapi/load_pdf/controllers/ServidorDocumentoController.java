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

import java.text.Normalizer;
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
            @RequestParam("files") List<MultipartFile> files) throws Exception {

        // Impede requisições sem arquivo ou com arquivo vazio (0 KB)
        if (files == null ||files.isEmpty()) {
            throw new BusinessException("Nenhum arquivo foi selecionado.");
        }
            // Envia para o Service (O mesmo método que você já tem pronto!)
        documentoService.attachDocumentsInBatch(servidorId, files);

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

    // MÉTODOS PRIVADOS
    // Limpa o nome do arquivo PDF enviado (retira caracteres especiais, etc)
    private String clearFileName(String nomeOriginal) {
        if (nomeOriginal == null || nomeOriginal.isBlank()) {
            return "documento.pdf";
        }

        // 1. Remove acentos (ex: "Relatório" vira "Relatorio")
        String nomeSemAcentos = Normalizer.normalize(nomeOriginal, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "");

        // 2. Substitui espaços e caracteres estranhos por underscore
        String nomeLimpo = nomeSemAcentos.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

        // 3. Truncar para proteger a coluna de auditoria (ex: máximo de 45 caracteres)
        if (nomeLimpo.length() > 45) {
            String extension = ".pdf";
            // Pega os primeiros 41 caracteres e adiciona os 4 do ".pdf" = 45 totais
            nomeLimpo = nomeLimpo.substring(0, 41) + extension;
        }

        return nomeLimpo.toLowerCase();
    }
}
