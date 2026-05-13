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
        AuditContextHolder.setFriendlyId(clearName);
        AuditContextHolder.setLogDetalhes("Upload de documento para o servidor: " + servidor.getNome());

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
        AuditContextHolder.setFriendlyId(documento.getOriginalName());
        AuditContextHolder.setLogDetalhes("Documento removido do cadastro do servidor: "
                + documento.getServidor().getNome());

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

    // MÉTODOS AUXLIARES
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


    public void attachDocumentsInBatch(Integer servidorId, List<MultipartFile> files)throws Exception {
        // 1. Laço de repetição: processa cada arquivo individualmente
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            // 2. Regra de Negócio: Validação de Extensão
            if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
                throw new BusinessException("O arquivo '" + file.getOriginalFilename() + "' não é um PDF válido.");
            }

            // 3. Regra de Negócio: Validação de Magic Numbers
            byte[] header = new byte[4];
            try {
                file.getInputStream().read(header);

                String magicNumber = new String(header);

                if (!magicNumber.equals("%PDF")) {
                    throw new BusinessException(
                            "O arquivo '" + file.getOriginalFilename() + "' não é um PDF válido ou está corrompido."
                    );
                }
            } catch (Exception e) {
                throw new BusinessException("Erro ao ler o arquivo '" + file.getOriginalFilename() + "'.");
            }

            // 3. Regra de Negócio: Higienização (Você pode mover aquele método privado para cá)
            String clearName = clearFileName(file.getOriginalFilename());

            // 4. Delega para o método que salva individualmente
            // Como esse método tem @Transactional, cada arquivo salvo é uma transação independente!
            this.attachDocument(servidorId, file, clearName);
        }

    }
}
