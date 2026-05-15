package br.gov.rn.natal.cadpgmapi.load_pdf.services;

import br.gov.rn.natal.cadpgmapi.audit.AuditContextHolder;
import br.gov.rn.natal.cadpgmapi.audit.annotations.Auditable;
import br.gov.rn.natal.cadpgmapi.audit.enums.AuditAction;
import br.gov.rn.natal.cadpgmapi.entity.Servidor;
import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import br.gov.rn.natal.cadpgmapi.exception.ResourceNotFoundException;
import br.gov.rn.natal.cadpgmapi.load_pdf.dtos.DocumentoResponseDTO;
import br.gov.rn.natal.cadpgmapi.load_pdf.entities.ServidorDocumento;
import br.gov.rn.natal.cadpgmapi.load_pdf.repositories.ServidorDocumentoRepository;
import br.gov.rn.natal.cadpgmapi.repository.ServidorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServidorDocumentoService {

    private final ServidorDocumentoRepository documentoRepository;
    private final ServidorRepository servidorRepository;
    private final DocumentoStorageService storageService; // Aquele do Passo 3 anterior

    // Construtor
    public ServidorDocumentoService(
            ServidorDocumentoRepository documentoRepository,
            ServidorRepository servidorRepository,
            DocumentoStorageService storageService) {
        this.documentoRepository = documentoRepository;
        this.servidorRepository = servidorRepository;
        this.storageService = storageService;
    }

    @Transactional
    @Auditable(action = AuditAction.INSERT, entity = "Documento Servidor")
    public void attachDocument(Integer servidorId, MultipartFile file, String clearName) throws Exception {
        Servidor servidor = servidorRepository.findById(servidorId)
                .orElseThrow(() -> new ResourceNotFoundException("Servidor não encontrado"));

        // 1. "Fofoca" para o Auditoria o que está acontecendo
        AuditContextHolder.setEntityName("Documento");
        AuditContextHolder.setFriendlyId(servidor.getNome());
        AuditContextHolder.setLogDetalhes("DOCUMENTO ANEXADO: " + clearName);

        String objectName = servidorId + "/" + UUID.randomUUID() + "-" + clearName;
        storageService.upload(file, objectName);

        ServidorDocumento document = ServidorDocumento.builder()
                .servidor(servidor)
                .originalName(clearName)
                .objectName(objectName)
                .contentType(file.getContentType())
                .bytesSize(file.getSize())
                .build();

        documentoRepository.save(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentoResponseDTO> listDocuments(Integer servidorId) {
        return documentoRepository.findByServidorId(servidorId).stream()
                .map(doc -> new DocumentoResponseDTO(
                        doc.getId(),
                        doc.getOriginalName(),
                        doc.getBytesSize(),
                        doc.getDataUpload()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    @Auditable(action = AuditAction.DELETE, entity = "Documento Servidor")
    public void deleteDocument(Integer documentId) throws Exception {
        ServidorDocumento documento = documentoRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado"));

        // 2. Prepara o log antes de excluir
        AuditContextHolder.setEntityName("Documento");
        AuditContextHolder.setFriendlyId(documento.getServidor().getNome());
        AuditContextHolder.setLogDetalhes("DOCUMENTO EXCLUÍDO: " + documento.getOriginalName());

        storageService.remove(documento.getObjectName());
        documentoRepository.delete(documento);
    }

    // Link
    @Transactional(readOnly = true)
    public String generateAccessLink(Integer documentId) throws Exception {
        ServidorDocumento documento = documentoRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado"));

        // Retorna o link pré-assinado do MinIO (com duração de 15 min)
        return storageService.getDownloadUrl(documento.getObjectName());
    }
}
