package br.gov.rn.natal.cadpgmapi.load_pdf.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record DocumentoResponseDTO(
        Integer id,
        String originalName,
        Long bytesSize,
        String formatedSize,

        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime dataUpload
) {
    // Construtor customizado (O Service vai chamar este construtor com 4 parâmetros)
    public DocumentoResponseDTO(
            Integer id,
            String originalName,
            Long bytesSize,
            LocalDateTime dataUpload)
    {
        // Repassa para o construtor principal gerando a String do tamanhoFormatado automaticamente
        this(id, originalName, bytesSize, sizeFormater(bytesSize), dataUpload);
    }

    // Método interno para calcular MB, KB ou Bytes
    private static String sizeFormater(Long bytes) {
        if (bytes == null) return "0 B";
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
