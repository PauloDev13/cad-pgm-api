package br.gov.rn.natal.cadpgmapi.load_pdf.services;

import br.gov.rn.natal.cadpgmapi.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.List;

@Service
public class DocumentoOrquestradorService {
    private final ServidorDocumentoService documentoService;

    public DocumentoOrquestradorService(ServidorDocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    /**
     * Este método agora é o "mestre de cerimônias".
     * Como ele chama o 'documentoService' de FORA, o Spring intercepta
     * a chamada e a auditoria funciona perfeitamente!
     */
    public void processBatchUpload(Integer servidorId, List<MultipartFile> files) throws Exception {
        // 1. Laço de repetição: processa cada arquivo individualmente
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            // 3. Regra de Negócio: Validação de Magic Numbers
            validateFile(file);

            // 3. Regra de Negócio: limpa o nome do arquivo de caracteres especiais, etc
            String clearName = clearFileName(file.getOriginalFilename());

            // 4. Delega para o método que salva individualmente
            // Como esse método tem @Transactional, cada arquivo salvo é uma transação independente!
            documentoService.attachDocument(servidorId, file, clearName);
        }
    }

    // Método para remoção de arquivos PDF em lote
    public void deleteDocumentsInBatch(List<Integer> documentsIds) {
        for (Integer id : documentsIds) {
            try{
                documentoService.deleteDocument(id);
            }catch (Exception e) {
                throw new BusinessException(
                        "Erro ao excluir o documento de ID: " + id +
                                ". Motivo: " + e.getMessage()
                );
            }
        }
    }


    // ******** MÉTODOS AUXILIARES ********
    // Limpa o nome do arquivo PDF enviado (retira caracteres especiais, etc)
    private String clearFileName(String nomeOriginal) {

        if (nomeOriginal == null || nomeOriginal.isBlank()) {
            return "documento.pdf";
        }

        // 1. Remove acentos (ex: "Relatório" vira "Relatório")
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

    private void validateFile(MultipartFile file) throws Exception {
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
    }
}
